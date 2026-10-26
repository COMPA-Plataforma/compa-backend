package com.compa.service;

import com.compa.dto.response.ExperienciaResponse;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.ExperienciaActividad;
import com.compa.model.Orientador;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.ExperienciaActividadRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// Consulta del psicoorientador: las experiencias que su estudiante conto sobre las actividades del plan.
@Service
@RequiredArgsConstructor
public class ExperienciaConsultaService {

    private final ExperienciaActividadRepository experienciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    // De la mas reciente a la mas antigua. Filtros opcionales: rango de fechas (incluye ambos
    // extremos) y actividad. Si no hay experiencias devuelve una lista vacia (la pantalla lo indica).
    @Transactional(readOnly = true)
    public List<ExperienciaResponse> listar(Long estudianteId, String orientadorEmail,
            LocalDate desde, LocalDate hasta, Long actividadId) {
        Orientador orientador = orientadorRepository.findByEmail(orientadorEmail)
                .orElseThrow(() -> new ForbiddenException(
                        "Solo un psicoorientador puede consultar las experiencias de un estudiante."));
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        verificarAcceso(estudiante, orientador);

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }

        return experienciaRepository.findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(estudianteId)
                .stream()
                .filter(e -> actividadId == null || e.getActividad().getId().equals(actividadId))
                .filter(e -> desde == null || !e.getFecha().isBefore(desde))
                .filter(e -> hasta == null || !e.getFecha().isAfter(hasta))
                .map(this::toResponse)
                .toList();
    }

    // Solo el psicoorientador asignado al estudiante puede consultarlas.
    private void verificarAcceso(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() == null) {
            throw new ForbiddenException(
                    "El estudiante aun no tiene psicoorientador asignado. Se asigna al registrar su atencion.");
        }
        if (!estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException(
                    "Solo el psicoorientador asignado puede consultar las experiencias de este estudiante.");
        }
    }

    private ExperienciaResponse toResponse(ExperienciaActividad e) {
        return ExperienciaResponse.builder()
                .id(e.getId())
                .actividadId(e.getActividad().getId())
                .actividadNombre(e.getActividad().getName())
                .texto(e.getTexto())
                .fecha(e.getFecha())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                // el psicoorientador solo lee: no corrige las experiencias del estudiante
                .editable(false)
                .build();
    }
}