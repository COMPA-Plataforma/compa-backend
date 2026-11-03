package com.compa.service;

import com.compa.dto.response.DerivacionEstudianteResponse;
import com.compa.enums.DestinoDerivacion;
import com.compa.exception.ForbiddenException;
import com.compa.model.Derivacion;
import com.compa.model.Estudiante;
import com.compa.repository.DerivacionRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.NovedadDerivacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DerivacionEstudianteService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private final EstudianteRepository estudianteRepository;
    private final DerivacionRepository derivacionRepository;
    private final NovedadDerivacionRepository novedadRepository;

    // Derivaciones del estudiante autenticado. El estudiante se identifica por el token,
    // asi que solo puede ver las suyas. Solo lectura.
    @Transactional(readOnly = true)
    public List<DerivacionEstudianteResponse> listarMias(String estudianteEmail) {
        Estudiante estudiante = estudianteRepository.findByEmail(estudianteEmail)
                .orElseThrow(() -> new ForbiddenException("Solo un estudiante puede consultar sus derivaciones."));

        LocalDate hoy = LocalDate.now(ZONA);
        return derivacionRepository.findByEstudianteIdOrderByFechaDescIdDesc(estudiante.getId()).stream()
                .map(d -> toResponse(d, hoy))
                .toList();
    }

    private DerivacionEstudianteResponse toResponse(Derivacion d, LocalDate hoy) {
        String nombre = d.getDestino() == DestinoDerivacion.OTRA
                ? d.getDestinoOtro()
                : d.getDestino().getEtiqueta();

        List<DerivacionEstudianteResponse.Novedad> novedades = novedadRepository
                .findByDerivacionIdOrderByFechaAscIdAsc(d.getId()).stream()
                .map(n -> DerivacionEstudianteResponse.Novedad.builder()
                        .fecha(n.getFecha())
                        .estado(n.getEstado())
                        .estadoEtiqueta(n.getEstado().getEtiqueta())
                        .build())
                .toList();

        return DerivacionEstudianteResponse.builder()
                .id(d.getId())
                .destinoNombre(nombre)
                .fecha(d.getFecha())
                .estado(d.getEstado())
                .estadoEtiqueta(d.getEstado().getEtiqueta())
                .diasTranscurridos(ChronoUnit.DAYS.between(d.getFecha(), hoy))
                .novedades(novedades)
                .build();
    }
}