package com.compa.service;


import com.compa.dto.request.ExperienciaRequest;
import com.compa.dto.response.ExperienciaResponse;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.ExperienciaActividad;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.ExperienciaActividadRepository;
import com.compa.repository.HabitPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExperienciaActividadService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final ExperienciaActividadRepository experienciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final HabitPlanRepository habitPlanRepository;

    // Registra una experiencia nueva sobre una actividad del plan activo.
    // Siempre crea una nueva: asi una actividad acumula varias experiencias en el tiempo.
    @Transactional
    public ExperienciaResponse registrar(String email, ExperienciaRequest request) {
        Estudiante estudiante = estudiante(email);

        // Sin plan activo no hay actividades sobre las que contar nada
        HabitPlan plan = habitPlanRepository.findByEstudianteIdAndStatus(estudiante.getId(), PlanStatus.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Aun no tienes un plan de acompanamiento activo"));

        if (request.getActividadId() == null) {
            throw new BusinessException("Elige la actividad sobre la que quieres contar tu experiencia");
        }
        HabitTask actividad = plan.getTasks().stream()
                .filter(t -> t.getId().equals(request.getActividadId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("La actividad no pertenece a tu plan activo"));

        ExperienciaActividad e = new ExperienciaActividad();
        e.setEstudiante(estudiante);
        e.setActividad(actividad);
        e.setTexto(request.getTexto().trim());
        e.setFecha(hoy());
        return toResponse(experienciaRepository.save(e));
    }

    // Corrige el texto de una experiencia, pero solo el mismo dia en que se registro
    @Transactional
    public ExperienciaResponse corregir(String email, Long experienciaId, ExperienciaRequest request) {
        Estudiante estudiante = estudiante(email);
        // Si la experiencia es de otro estudiante responde 404, como si no existiera
        ExperienciaActividad e = experienciaRepository.findByIdAndEstudianteId(experienciaId, estudiante.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Experiencia", experienciaId));

        if (!e.getFecha().equals(hoy())) {
            throw new BusinessException("Solo puedes corregir una experiencia el mismo dia en que la registraste");
        }
        e.setTexto(request.getTexto().trim());
        e.setUpdatedAt(LocalDateTime.now());
        return toResponse(experienciaRepository.save(e));
    }

    // Mis experiencias, de la mas reciente a la mas antigua. Opcionalmente de una sola actividad.
    @Transactional(readOnly = true)
    public List<ExperienciaResponse> listar(String email, Long actividadId) {
        Estudiante estudiante = estudiante(email);
        List<ExperienciaActividad> lista = actividadId == null
                ? experienciaRepository.findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(estudiante.getId())
                : experienciaRepository.findByEstudianteIdAndActividadIdOrderByFechaDescCreatedAtDescIdDesc(
                        estudiante.getId(), actividadId);
        return lista.stream().map(this::toResponse).toList();
    }

    // ---------- helpers ----------

    private Estudiante estudiante(String email) {
        return estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
    }

    private LocalDate hoy() {
        return LocalDate.now(BOGOTA);
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
                .editable(e.getFecha().equals(hoy()))
                .build();
    }
}