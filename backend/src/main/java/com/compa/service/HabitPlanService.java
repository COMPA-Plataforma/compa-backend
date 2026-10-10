package com.compa.service;


import com.compa.dto.request.HabitPlanRequest;
import com.compa.dto.request.HabitTaskRequest;
import com.compa.dto.response.PlanEstudianteResponse;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.repository.AtencionRepository;
import com.compa.repository.HabitPlanRepository;
import com.compa.repository.HabitTaskRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;


@Service
public class HabitPlanService {

    private final HabitPlanRepository habitPlanRepository;
    private final HabitTaskRepository habitTaskRepository;
    private final EstudianteRepository estudianteRepository;
    private final AtencionRepository atencionRepository;
    private final OrientadorRepository orientadorRepository;

    // Las fechas de sesión se calculan con la hora de Colombia, no con la del servidor
    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    public HabitPlanService(HabitPlanRepository habitPlanRepository, HabitTaskRepository habitTaskRepository,
                            EstudianteRepository estudianteRepository, AtencionRepository atencionRepository,
                            OrientadorRepository orientadorRepository) {
        this.habitPlanRepository = habitPlanRepository;
        this.habitTaskRepository = habitTaskRepository;
        this.estudianteRepository = estudianteRepository;
        this.atencionRepository = atencionRepository;
        this.orientadorRepository = orientadorRepository;
    }

    // Crea el plan con las actividades acordadas en la sesión. Requiere atención registrada,
    // al menos una actividad y que el estudiante no tenga ya un plan activo.
    @Transactional
    public HabitPlan createPlan(Long estudianteId, String orientadorEmail, HabitPlanRequest request) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        verificarOrientadorAsignado(estudiante, orientador);

        if (!atencionRepository.existsByEstudianteId(estudianteId)) {
            throw new BusinessException(
                    "Solo se puede crear el plan si el estudiante tiene una atencion registrada.");
        }

        if (habitPlanRepository.existsByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)) {
            throw new BusinessException(
                    "El estudiante ya tiene un plan activo. Desactiva el plan actual antes de crear uno nuevo.");
        }

        if (request.getTasks() == null || request.getTasks().isEmpty()) {
            throw new BusinessException("El plan debe crearse con al menos una actividad.");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        LocalDate fechaSesion = fechaSesion(request.getSessionDate());

        HabitPlan plan = new HabitPlan();
        plan.setEstudiante(estudiante);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setAgreedDate(fechaSesion);
        plan.setStatus(PlanStatus.ACTIVO);

        // Las actividades se guardan junto con el plan (cascade) y quedan en su lista
        for (HabitTaskRequest taskRequest : request.getTasks()) {
            plan.getTasks().add(construirActividad(plan, taskRequest, fechaSesion));
        }
        return habitPlanRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<HabitPlan> getPlansByEstudiante(Long estudianteId) {
        estudianteRepository.findById(estudianteId).orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        return habitPlanRepository.findByEstudianteId(estudianteId);
    }

    @Transactional(readOnly = true)
    public HabitPlan getActivePlan(Long estudianteId) {
        estudianteRepository.findById(estudianteId).orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        return habitPlanRepository.findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante no tiene un plan activo"));
    }

    // Vista de solo lectura para el estudiante: su plan activo con las actividades,
    // su frecuencia y la fecha en que se acordaron. Si no tiene plan activo, responde 404
    // con un mensaje que la pantalla puede mostrarle.
    @Transactional(readOnly = true)
    public PlanEstudianteResponse getPlanActivoParaEstudiante(Long estudianteId) {
        HabitPlan plan = habitPlanRepository.findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("Aun no tienes un plan de acompanamiento activo"));

        List<PlanEstudianteResponse.Actividad> actividades = plan.getTasks().stream()
                .sorted(Comparator.comparing(HabitTask::getCreatedAt).thenComparing(HabitTask::getId))
                .map(t -> PlanEstudianteResponse.Actividad.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .priority(t.getPriority())
                        .mandatory(t.isMandatory())
                        .weeklyGoal(t.getWeeklyGoal())
                        .specificDays(new ArrayList<>(t.getSpecificDays()))
                        .dueDate(t.getDueDate())
                        // actividades antiguas no tienen fecha de sesión: se usa la de creación
                        .agreedDate(t.getAgreedDate() != null ? t.getAgreedDate() : t.getCreatedAt().toLocalDate())
                        .createdAt(t.getCreatedAt())
                        .build())
                .toList();

        return PlanEstudianteResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .status(plan.getStatus())
                .createdAt(plan.getCreatedAt())
                .agreedDate(plan.getAgreedDate() != null ? plan.getAgreedDate() : plan.getCreatedAt().toLocalDate())
                .tasks(actividades)
                .build();
    }

    @Transactional(readOnly = true)
    public HabitPlan getPlanById(Long planId) {
        return habitPlanRepository.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
    }

    @Transactional
    public HabitPlan updatePlan(Long planId, HabitPlanRequest request) {
        HabitPlan plan = habitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        return habitPlanRepository.save(plan);
    }

    @Transactional
    public HabitPlan deactivatePlan(Long planId) {
        HabitPlan plan = habitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        if (PlanStatus.INACTIVO.equals(plan.getStatus())) {
            throw new BusinessException("El plan ya se encuentra inactivo.");
        }
        plan.setStatus(PlanStatus.INACTIVO);
        return habitPlanRepository.save(plan);
    }

    // Agrega una actividad acordada en una sesión posterior, con la fecha de esa sesión.
    @Transactional
    public HabitTask addTask(Long estudianteId, Long planId, String orientadorEmail, HabitTaskRequest request) {
        Orientador orientador = orientadorActual(orientadorEmail);
        HabitPlan plan = habitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        // El plan debe ser del estudiante de la ruta
        if (!plan.getEstudiante().getId().equals(estudianteId)) {
            throw new ResourceNotFoundException("Plan", planId);
        }
        verificarOrientadorAsignado(plan.getEstudiante(), orientador);

        if (!PlanStatus.ACTIVO.equals(plan.getStatus())) {
            throw new BusinessException("No se pueden agregar actividades a un plan inactivo.");
        }

        LocalDate fechaSesion = fechaSesion(request.getSessionDate());
        if (plan.getAgreedDate() != null && fechaSesion.isBefore(plan.getAgreedDate())) {
            throw new BusinessException("La fecha de la sesion no puede ser anterior a la del plan.");
        }
        return habitTaskRepository.save(construirActividad(plan, request, fechaSesion));
    }

    // ---------- helpers ----------

    private Orientador orientadorActual(String email) {
        return orientadorRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede gestionar planes."));
    }

    // Si el estudiante ya está asignado a otro psicoorientador, este no puede gestionar su plan.
    private void verificarOrientadorAsignado(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() != null
                && !estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException("Solo el psicoorientador asignado puede gestionar el plan de este estudiante.");
        }
    }

    // Fecha de la sesión: la enviada (no puede ser futura) o, si no viene, hoy en hora de Colombia.
    private LocalDate fechaSesion(LocalDate enviada) {
        LocalDate hoy = LocalDate.now(ZONA);
        if (enviada == null) {
            return hoy;
        }
        if (enviada.isAfter(hoy)) {
            throw new BusinessException("La fecha de la sesion no puede ser futura.");
        }
        return enviada;
    }

    // Arma la actividad (sin guardarla) con descripción, frecuencia o fecha límite y la fecha de sesión.
    private HabitTask construirActividad(HabitPlan plan, HabitTaskRequest request, LocalDate fechaSesion) {
        if (request.getDueDate() != null && request.getDueDate().isBefore(fechaSesion)) {
            throw new BusinessException(
                    "La fecha limite de una actividad no puede ser anterior a la fecha de la sesion.");
        }
        HabitTask task = new HabitTask();
        task.setHabitPlan(plan);
        task.setName(request.getName().trim());
        task.setDescription(request.getDescription().trim());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setMandatory(request.isMandatory());
        task.setWeeklyGoal(request.getWeeklyGoal());
        task.setSpecificDays(request.getSpecificDays() != null
                ? new ArrayList<>(request.getSpecificDays())
                : new ArrayList<>());
        task.setDueDate(request.getDueDate());
        task.setAgreedDate(fechaSesion);
        return task;
    }

    @Transactional(readOnly = true)
    public List<HabitTask> getTasksForToday(Long estudianteId) {
        String todayRaw = LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("es", "CO"));
        final String today = todayRaw.substring(0, 1).toUpperCase() + todayRaw.substring(1);

        Optional<HabitPlan> activePlan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO);

        if (activePlan.isEmpty()) {
            return new ArrayList<>();
        }

        return activePlan.get().getTasks()
                .stream()
                .filter(task -> task.getSpecificDays().contains(today))
                .collect(Collectors.toList());
    }



    @Transactional
    public void deleteTask(Long taskId) {
        HabitTask task = habitTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));
        habitTaskRepository.delete(task);
    }
}