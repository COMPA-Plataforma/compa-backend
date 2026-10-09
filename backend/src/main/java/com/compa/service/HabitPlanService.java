package com.compa.service;


import com.compa.dto.request.HabitPlanRequest;
import com.compa.dto.request.HabitTaskRequest;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.model.Estudiante;
import com.compa.repository.HabitPlanRepository;
import com.compa.repository.HabitTaskRepository;
import com.compa.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import com.compa.dto.response.PlanEstudianteResponse;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
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

    public HabitPlanService(HabitPlanRepository habitPlanRepository, HabitTaskRepository habitTaskRepository,
                            EstudianteRepository estudianteRepository) {
        this.habitPlanRepository = habitPlanRepository;
        this.habitTaskRepository = habitTaskRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional
    public HabitPlan createPlan(Long estudianteId, HabitPlanRequest request) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        if (habitPlanRepository.existsByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)) {
            throw new BusinessException(
                    "El estudiante ya tiene un plan activo. Desactiva el plan actual antes de crear uno nuevo.");
        }

        if (request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        HabitPlan plan = new HabitPlan();
        plan.setEstudiante(estudiante);
        plan.setName(request.getName());
        plan.setDescription(request.getDescription());
        plan.setStartDate(request.getStartDate());
        plan.setEndDate(request.getEndDate());
        plan.setStatus(PlanStatus.ACTIVO);
        plan = habitPlanRepository.save(plan);

        if (request.getTasks() != null) {
            for (HabitTaskRequest taskRequest : request.getTasks()) {
                HabitTask task = new HabitTask();
                task.setHabitPlan(plan);
                task.setName(taskRequest.getName());
                task.setDescription(taskRequest.getDescription());
                habitTaskRepository.save(task);
            }
        }
        HabitPlan savedPlan = habitPlanRepository.findById(plan.getId()).orElse(plan);
        savedPlan.getTasks().size(); // fuerza la carga de las tareas
        return savedPlan;
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

    @Transactional
    public HabitTask addTask(Long planId, HabitTaskRequest request) {
        HabitPlan plan = habitPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId));
        HabitTask task = new HabitTask();
        task.setHabitPlan(plan);
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setMandatory(request.isMandatory());
        task.setWeeklyGoal(request.getWeeklyGoal());
        task.setSpecificDays(request.getSpecificDays());
        return habitTaskRepository.save(task);
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