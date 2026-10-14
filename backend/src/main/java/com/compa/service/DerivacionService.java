package com.compa.service;

import com.compa.dto.request.DerivacionRequest;
import com.compa.dto.response.DerivacionResponse;
import com.compa.enums.AlertStatus;
import com.compa.enums.DestinoDerivacion;
import com.compa.enums.EstadoDerivacion;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Alert;
import com.compa.model.Derivacion;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.repository.AlertRepository;
import com.compa.repository.DerivacionRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DerivacionService {

    private final DerivacionRepository derivacionRepository;
    private final AlertRepository alertRepository;
    private final OrientadorRepository orientadorRepository;

    // Deriva al estudiante desde una alerta activa. Todo en una transaccion:
    // se crea la derivacion (DERIVADO) y la alerta pasa a ATENDIDA_POR_DERIVACION, o no pasa nada.
    @Transactional
    public DerivacionResponse derivar(Long alertId, String orientadorEmail, DerivacionRequest request) {
        Orientador orientador = orientadorRepository.findByEmail(orientadorEmail)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede derivar a un estudiante."));
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta", alertId));
        Estudiante estudiante = alert.getEstudiante();

        verificarAcceso(estudiante, orientador);

        if (alert.getStatus() != AlertStatus.PENDIENTE) {
            throw new BusinessException("La alerta ya fue atendida y no admite una nueva derivacion");
        }

        String destinoOtro = null;
        if (request.getDestino() == DestinoDerivacion.OTRA) {
            destinoOtro = request.getDestinoOtro() == null ? "" : request.getDestinoOtro().trim();
            if (destinoOtro.isEmpty()) {
                throw new BusinessException("Indica el nombre de la ruta de atencion");
            }
        }

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        LocalDate fecha = request.getFecha() != null ? request.getFecha() : hoy;
        if (fecha.isAfter(hoy)) {
            throw new BusinessException("La fecha de la derivacion no puede ser futura");
        }

        Derivacion d = new Derivacion();
        d.setEstudiante(estudiante);
        d.setOrientador(orientador);
        d.setAlert(alert);
        d.setDestino(request.getDestino());
        d.setDestinoOtro(destinoOtro);
        d.setFecha(fecha);
        d.setMotivo(request.getMotivo().trim());
        d.setEstado(EstadoDerivacion.DERIVADO);
        d = derivacionRepository.save(d);

        alert.setStatus(AlertStatus.ATENDIDA_POR_DERIVACION);
        alert.setResolvedAt(LocalDateTime.now());
        alertRepository.save(alert);

        return toResponse(d, alert);
    }

    // Solo el psicoorientador asignado al estudiante puede derivarlo.
    private void verificarAcceso(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() == null
                || !estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException("Solo el psicoorientador asignado puede derivar a este estudiante.");
        }
    }

    private DerivacionResponse toResponse(Derivacion d, Alert alert) {
        Orientador o = d.getOrientador();
        String nombre = d.getDestino() == DestinoDerivacion.OTRA
                ? d.getDestinoOtro()
                : d.getDestino().getEtiqueta();
        return DerivacionResponse.builder()
                .id(d.getId())
                .estudianteId(d.getEstudiante().getId())
                .alertId(alert.getId())
                .alertStatus(alert.getStatus())
                .destino(d.getDestino())
                .destinoNombre(nombre)
                .fecha(d.getFecha())
                .motivo(d.getMotivo())
                .estado(d.getEstado())
                .orientadorId(o.getId())
                .orientadorNombre(o.getName() + " " + o.getLastName())
                .createdAt(d.getCreatedAt())
                .build();
    }
}