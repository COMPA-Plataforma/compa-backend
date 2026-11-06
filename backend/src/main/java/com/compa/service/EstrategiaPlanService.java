package com.compa.service;

import com.compa.dto.request.EstrategiaRequest;
import com.compa.dto.response.EstrategiaResponse;
import com.compa.enums.EstadoEstrategia;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.EstrategiaPlan;
import com.compa.model.HabitPlan;
import com.compa.repository.EstrategiaPlanRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.HabitPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

// Estrategias que el psicoorientador trabaja con el estudiante, ligadas a su plan de acompañamiento activo.
// El acceso del orientador al estudiante lo valida EstudianteAccessInterceptor.
@Service
@RequiredArgsConstructor
public class EstrategiaPlanService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final EstrategiaPlanRepository estrategiaRepository;
    private final EstudianteRepository estudianteRepository;
    private final HabitPlanRepository habitPlanRepository;

    // ───────────── Psicoorientador ─────────────

    @Transactional
    public EstrategiaResponse crear(Long estudianteId, EstrategiaRequest request) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        HabitPlan plan = habitPlanRepository.findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new BusinessException(
                        "El estudiante no tiene un plan de acompañamiento activo. Crea el plan antes de registrar estrategias."));

        EstrategiaPlan e = new EstrategiaPlan();
        e.setEstudiante(estudiante);
        e.setPlan(plan);
        e.setDescripcion(request.getDescripcion().trim());
        e.setFechaSesion(request.getFechaSesion());
        e.setEstado(EstadoEstrategia.VIGENTE);
        return toResponse(estrategiaRepository.save(e));
    }

    // Historial del estudiante en orden cronológico, con las anuladas incluidas
    @Transactional(readOnly = true)
    public List<EstrategiaResponse> historial(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        return estrategiaRepository.findByEstudianteIdOrderByFechaSesionAscIdAsc(estudianteId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public EstrategiaResponse actualizar(Long estudianteId, Long estrategiaId, EstrategiaRequest request) {
        EstrategiaPlan e = estrategiaDelEstudiante(estudianteId, estrategiaId);
        if (e.getEstado() == EstadoEstrategia.ANULADA) {
            throw new BusinessException("Una estrategia anulada no se puede modificar.");
        }
        e.setDescripcion(request.getDescripcion().trim());
        e.setFechaSesion(request.getFechaSesion());
        e.setActualizadaEn(LocalDateTime.now(BOGOTA));
        return toResponse(estrategiaRepository.save(e));
    }

    // "Eliminar" = anular: la estrategia se conserva en el historial
    @Transactional
    public EstrategiaResponse anular(Long estudianteId, Long estrategiaId) {
        EstrategiaPlan e = estrategiaDelEstudiante(estudianteId, estrategiaId);
        if (e.getEstado() == EstadoEstrategia.ANULADA) {
            throw new BusinessException("Esta estrategia ya está anulada.");
        }
        e.setEstado(EstadoEstrategia.ANULADA);
        e.setAnuladaEn(LocalDateTime.now(BOGOTA));
        return toResponse(estrategiaRepository.save(e));
    }

    // ───────────── Estudiante ─────────────

    // Solo las vigentes del plan activo, en orden cronológico
    @Transactional(readOnly = true)
    public List<EstrategiaResponse> vigentesDelPlanActivo(String estudianteEmail) {
        Estudiante estudiante = estudianteRepository.findByEmail(estudianteEmail)
                .orElseThrow(() -> new ForbiddenException("Solo un estudiante puede ver sus estrategias."));
        return habitPlanRepository.findByEstudianteIdAndStatus(estudiante.getId(), PlanStatus.ACTIVO)
                .map(plan -> estrategiaRepository
                        .findByPlanIdAndEstadoOrderByFechaSesionAscIdAsc(plan.getId(), EstadoEstrategia.VIGENTE)
                        .stream().map(this::toResponse).toList())
                .orElse(List.of());
    }

    // ───────────── Internos ─────────────

    // La estrategia debe ser del estudiante de la ruta (evita tocar la de otro estudiante cambiando el id)
    private EstrategiaPlan estrategiaDelEstudiante(Long estudianteId, Long estrategiaId) {
        EstrategiaPlan e = estrategiaRepository.findById(estrategiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Estrategia", estrategiaId));
        if (!e.getEstudiante().getId().equals(estudianteId)) {
            throw new ResourceNotFoundException("Estrategia", estrategiaId);
        }
        return e;
    }

    private EstrategiaResponse toResponse(EstrategiaPlan e) {
        return EstrategiaResponse.builder()
                .id(e.getId())
                .planId(e.getPlan().getId())
                .planNombre(e.getPlan().getName())
                .descripcion(e.getDescripcion())
                .fechaSesion(e.getFechaSesion())
                .estado(e.getEstado())
                .creadaEn(e.getCreadaEn())
                .actualizadaEn(e.getActualizadaEn())
                .anuladaEn(e.getAnuladaEn())
                .build();
    }
}