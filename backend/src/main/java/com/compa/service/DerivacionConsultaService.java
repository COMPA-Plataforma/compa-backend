package com.compa.service;

import com.compa.dto.response.DerivacionHistorialResponse;
import com.compa.dto.response.NovedadDerivacionResponse;
import com.compa.enums.DestinoDerivacion;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Derivacion;
import com.compa.model.Estudiante;
import com.compa.model.NovedadDerivacion;
import com.compa.model.Orientador;
import com.compa.repository.DerivacionRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.NovedadDerivacionRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DerivacionConsultaService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final DerivacionRepository derivacionRepository;
    private final NovedadDerivacionRepository novedadRepository;
    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    // Derivaciones de un estudiante con sus novedades. Solo su psicoorientador asignado.
    @Transactional(readOnly = true)
    public List<DerivacionHistorialResponse> listarPorEstudiante(Long estudianteId, String orientadorEmail) {
        Orientador orientador = buscarOrientador(orientadorEmail);
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        if (estudiante.getOrientador() == null
                || !estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException("Solo el psicoorientador asignado puede consultar las derivaciones de este estudiante.");
        }

        LocalDate hoy = LocalDate.now(ZONA);
        return derivacionRepository.findByEstudianteIdOrderByFechaDescIdDesc(estudianteId).stream()
                .map(d -> toHistorial(d, hoy))
                .toList();
    }

    // Derivaciones de todos los estudiantes asignados al psicoorientador (vista general)
    @Transactional(readOnly = true)
    public List<DerivacionHistorialResponse> listarMias(String orientadorEmail) {
        Orientador orientador = buscarOrientador(orientadorEmail);
        LocalDate hoy = LocalDate.now(ZONA);
        return derivacionRepository.findByEstudiante_Orientador_IdOrderByFechaDescIdDesc(orientador.getId()).stream()
                .map(d -> toHistorial(d, hoy))
                .toList();
    }

    private Orientador buscarOrientador(String email) {
        return orientadorRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede consultar las derivaciones."));
    }

    private DerivacionHistorialResponse toHistorial(Derivacion d, LocalDate hoy) {
        long dias = ChronoUnit.DAYS.between(d.getFecha(), hoy);
        String nombre = d.getDestino() == DestinoDerivacion.OTRA
                ? d.getDestinoOtro()
                : d.getDestino().getEtiqueta();
        Orientador o = d.getOrientador();
        Estudiante e = d.getEstudiante();

        List<NovedadDerivacionResponse> novedades = novedadRepository
                .findByDerivacionIdOrderByFechaAscIdAsc(d.getId()).stream()
                .map(n -> toNovedad(n, d, dias))
                .toList();

        return DerivacionHistorialResponse.builder()
                .id(d.getId())
                .estudianteId(e.getId())
                .estudianteNombre(e.getName() + " " + e.getLastName())
                .destino(d.getDestino())
                .destinoNombre(nombre)
                .fecha(d.getFecha())
                .motivo(d.getMotivo())
                .estado(d.getEstado())
                .estadoEtiqueta(d.getEstado().getEtiqueta())
                .diasTranscurridos(dias)
                .orientadorNombre(o.getName() + " " + o.getLastName())
                .novedades(novedades)
                .build();
    }

    private NovedadDerivacionResponse toNovedad(NovedadDerivacion n, Derivacion d, long dias) {
        Orientador o = n.getOrientador();
        return NovedadDerivacionResponse.builder()
                .id(n.getId())
                .derivacionId(d.getId())
                .fecha(n.getFecha())
                .descripcion(n.getDescripcion())
                .estado(n.getEstado())
                .estadoEtiqueta(n.getEstado().getEtiqueta())
                .estadoActual(d.getEstado())
                .diasTranscurridos(dias)
                .orientadorId(o.getId())
                .orientadorNombre(o.getName() + " " + o.getLastName())
                .createdAt(n.getCreatedAt())
                .build();
    }
}