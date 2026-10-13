package com.compa.service;

import com.compa.dto.request.DailyCheckInRequest;
import com.compa.dto.request.TaskCheckInRequest;
import com.compa.dto.response.CheckInDetailResponse;
import com.compa.dto.response.CheckInSummaryResponse;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.*;
import com.compa.repository.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DailyCheckInService {
    private final DailyCheckInRepository checkInRepository;
    private final TaskCheckInRepository taskCheckInRepository;
    private final EstudianteRepository estudianteRepository;
    private final HabitPlanRepository habitPlanRepository;
    private final HabitTaskRepository habitTaskRepository;
    private final AdherenceService adherenceService;
    private final RuleEvaluationService ruleEvaluationService;

    public DailyCheckInService(DailyCheckInRepository checkInRepository,
                               TaskCheckInRepository taskCheckInRepository,
                               EstudianteRepository estudianteRepository,
                               HabitPlanRepository habitPlanRepository,
                               HabitTaskRepository habitTaskRepository,
                               AdherenceService adherenceService,
                               @Lazy RuleEvaluationService ruleEvaluationService) {
        this.checkInRepository = checkInRepository;
        this.taskCheckInRepository = taskCheckInRepository;
        this.estudianteRepository = estudianteRepository;
        this.habitPlanRepository = habitPlanRepository;
        this.habitTaskRepository = habitTaskRepository;
        this.adherenceService = adherenceService;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    public List<com.compa.enums.EmotionalState> getEmotionalStates() {
        return List.of(com.compa.enums.EmotionalState.values());
    }

    @Transactional(readOnly = true)
    public List<HabitTask> getTodayTasks(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        HabitPlan activePlan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante no tiene un plan activo"));

        return habitTaskRepository.findByHabitPlanId(activePlan.getId());
    }

    @Transactional
    public DailyCheckIn createCheckIn(Long estudianteId, DailyCheckInRequest request) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        validateTasksBelongToActivePlan(estudianteId, request.getTasks());   // <-- NUEVA


        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        DailyCheckIn checkIn = checkInRepository
                .findByEstudianteIdAndCheckInDate(estudianteId, today)
                .orElse(null);

        // Un registro puede existir sin estado de ánimo si el estudiante ya
        // completó una tarea desde una notificación (HU-03) antes de abrir la
        // app. En ese caso no es "ya hiciste tu check-in hoy": se completa
        // ese mismo registro en vez de bloquear o duplicar.
        if (checkIn != null && checkIn.getEmotionalState() != null) {
            throw new BusinessException("Ya realizaste tu registro de hoy. Puedes editarlo hasta las 23:59.");
        }

        if (checkIn == null) {
            checkIn = new DailyCheckIn();
            checkIn.setEstudiante(estudiante);
            checkIn.setCheckInDate(today);
        }
        checkIn.setEmotionalState(request.getEmotionalState());
        checkIn = checkInRepository.save(checkIn);

        if (request.getTasks() != null) {
            for (TaskCheckInRequest taskRequest : request.getTasks()) {
                HabitTask task = habitTaskRepository.findById(taskRequest.getTaskId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskRequest.getTaskId()));

                // Upsert: si la tarea ya tenía un TaskCheckIn (p.ej. creado por
                // la acción rápida de una notificación), se actualiza en vez
                // de duplicarlo.
                TaskCheckIn taskCheckIn = taskCheckInRepository
                        .findByCheckInIdAndTaskId(checkIn.getId(), taskRequest.getTaskId())
                        .orElseGet(TaskCheckIn::new);
                taskCheckIn.setCheckIn(checkIn);
                taskCheckIn.setTask(task);
                taskCheckIn.setCompleted(taskRequest.isCompleted());
                taskCheckIn.setComment(normalizeComment(taskRequest.getComment()));
                taskCheckIn.setBarrier(taskRequest.getBarrier());
                taskCheckInRepository.save(taskCheckIn);
            }
        }

        // Calcular métricas y evaluar reglas
        adherenceService.calculateAndSave(estudianteId);
        ruleEvaluationService.evaluateRulesForEstudiante(estudianteId);

        return checkInRepository.findById(checkIn.getId()).orElse(checkIn);
    }

    @Transactional
    public DailyCheckIn updateCheckIn(Long estudianteId, DailyCheckInRequest request) {

        validateTasksBelongToActivePlan(estudianteId, request.getTasks());   // <-- NUEVA

        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        DailyCheckIn checkIn = checkInRepository
                .findByEstudianteIdAndCheckInDate(estudianteId, today)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el registro de hoy"));

        LocalDateTime limit = today.atTime(23, 59);
        if (LocalDateTime.now().isAfter(limit)) {
            throw new BusinessException("Ya no puedes editar el registro de hoy.");
        }

        checkIn.setEmotionalState(request.getEmotionalState());
        checkIn = checkInRepository.save(checkIn);

        List<TaskCheckIn> existing = taskCheckInRepository.findByCheckInId(checkIn.getId());
        taskCheckInRepository.deleteAll(existing);

        if (request.getTasks() != null) {
            for (TaskCheckInRequest taskRequest : request.getTasks()) {
                HabitTask task = habitTaskRepository.findById(taskRequest.getTaskId())
                        .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskRequest.getTaskId()));

                TaskCheckIn taskCheckIn = new TaskCheckIn();
                taskCheckIn.setCheckIn(checkIn);
                taskCheckIn.setTask(task);
                taskCheckIn.setCompleted(taskRequest.isCompleted());
                taskCheckIn.setBarrier(taskRequest.getBarrier());
                taskCheckIn.setComment(normalizeComment(taskRequest.getComment()));
                taskCheckInRepository.save(taskCheckIn);
            }
        }

        // Recalcular métricas y re-evaluar reglas
        adherenceService.calculateAndSave(estudianteId);
        ruleEvaluationService.evaluateRulesForEstudiante(estudianteId);

        return checkInRepository.findById(checkIn.getId()).orElse(checkIn);
    }

    /**
     * Marca una tarea puntual como cumplida para el día de hoy, sin requerir
     * que el estudiante haya completado el resto del check-in (emocional u
     * otras tareas). Usado por la acción rápida "Marcar cumplida" desde una
     * notificación (HU-03).
     */
    @Transactional
    public TaskCheckIn markTaskCompleted(Long estudianteId, Long taskId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        HabitTask task = habitTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea", taskId));

        if (!task.getHabitPlan().getEstudiante().getId().equals(estudianteId)) {
            throw new ResourceNotFoundException("Tarea", taskId);
        }

        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        DailyCheckIn checkIn = checkInRepository
                .findByEstudianteIdAndCheckInDate(estudianteId, today)
                .orElseGet(() -> {
                    DailyCheckIn newCheckIn = new DailyCheckIn();
                    newCheckIn.setEstudiante(estudiante);
                    newCheckIn.setCheckInDate(today);
                    return checkInRepository.save(newCheckIn);
                });

        TaskCheckIn taskCheckIn = taskCheckInRepository
                .findByCheckInIdAndTaskId(checkIn.getId(), taskId)
                .orElseGet(TaskCheckIn::new);

        taskCheckIn.setCheckIn(checkIn);
        taskCheckIn.setTask(task);
        taskCheckIn.setCompleted(true);
        taskCheckIn.setBarrier(null);
        taskCheckIn = taskCheckInRepository.save(taskCheckIn);

        adherenceService.calculateAndSave(estudianteId);
        ruleEvaluationService.evaluateRulesForEstudiante(estudianteId);

        return taskCheckIn;
    }

    @Transactional(readOnly = true)
    public DailyCheckIn getTodayCheckIn(Long estudianteId) {
        return checkInRepository
                .findByEstudianteIdAndCheckInDate(estudianteId, LocalDate.now(java.time.ZoneId.of("America/Bogota")))
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro el registro de hoy"));
    }

    @Transactional(readOnly = true)
    public int getCurrentStreak(Long estudianteId) {
        List<DailyCheckIn> checkIns = checkInRepository
                .findByEstudianteIdOrderByCheckInDateDesc(estudianteId);

        if (checkIns.isEmpty())
            return 0;

        int streak = 0;
        LocalDate expected = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        for (DailyCheckIn checkIn : checkIns) {
            if (checkIn.getCheckInDate().equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    public String getClosingMessage(Long estudianteId) {
        int streak = getCurrentStreak(estudianteId);
        if (streak == 1)
            return "Primer dia completado. Cada gran habito empieza con un primer paso.";
        if (streak < 4)
            return "Llevas " + streak + " dias seguidos. Lo estas logrando!";
        if (streak < 7)
            return "Increible! " + streak + " dias de racha. Estas construyendo un habito real.";
        if (streak < 14)
            return "Una semana completa! " + streak + " dias seguidos. Eres constante.";
        return "Llevas " + streak + " dias consecutivos. Eres una inspiracion para ti mismo.";
    }

    @Transactional(readOnly = true)
    public List<DailyCheckIn> getHistory(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        return checkInRepository.findByEstudianteIdOrderByCheckInDateDesc(estudianteId);
    }

    @Transactional(readOnly = true)
    public List<CheckInSummaryResponse> getLast30Days(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Bogota"));

        List<DailyCheckIn> checkIns = checkInRepository
                .findByEstudianteIdOrderByCheckInDateDesc(estudianteId);

        Map<LocalDate, DailyCheckIn> checkInMap = checkIns.stream()
                .collect(java.util.stream.Collectors.toMap(
                        DailyCheckIn::getCheckInDate,
                        c -> c,
                        (a, b) -> a));

        List<CheckInSummaryResponse> result = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.minusDays(i);
            DailyCheckIn checkIn = checkInMap.get(date);

            if (checkIn != null) {
                boolean hasMood = checkIn.getEmotionalState() != null;
                result.add(CheckInSummaryResponse.builder()
                        .date(date)
                        .status("COMPLETADO")
                        .emotionalState(checkIn.getEmotionalState())
                        .emotionalStateIcon(hasMood ? checkIn.getEmotionalState().getIcon() : null)
                        .checkInId(checkIn.getId())
                        .build());
            } else {
                result.add(CheckInSummaryResponse.builder()
                        .date(date)
                        .status("NO_REGISTRADO")
                        .emotionalState(null)
                        .emotionalStateIcon(null)
                        .checkInId(null)
                        .build());
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public CheckInDetailResponse getCheckInDetail(Long estudianteId, Long checkInId) {
        DailyCheckIn checkIn = checkInRepository.findById(checkInId)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in", checkInId));

        if (!checkIn.getEstudiante().getId().equals(estudianteId)) {
            throw new ResourceNotFoundException("Check-in", checkInId);
        }

        List<TaskCheckIn> taskCheckIns = taskCheckInRepository.findByCheckInId(checkInId);

        List<CheckInDetailResponse.TaskDetailResponse> taskDetails = taskCheckIns.stream()
                .map(t -> CheckInDetailResponse.TaskDetailResponse.builder()
                        .taskId(t.getTask().getId())
                        .taskName(t.getTask().getName())
                        .taskDescription(t.getTask().getDescription())
                        .completed(t.isCompleted())
                        .barrier(t.getBarrier())
                        .barrierLabel(t.getBarrier() != null ? t.getBarrier().getDisplayName() : null)
                        .comment(t.getComment())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        boolean hasMood = checkIn.getEmotionalState() != null;
        return CheckInDetailResponse.builder()
                .id(checkIn.getId())
                .checkInDate(checkIn.getCheckInDate())
                .emotionalState(checkIn.getEmotionalState())
                .emotionalStateIcon(hasMood ? checkIn.getEmotionalState().getIcon() : null)
                .emotionalStateLabel(hasMood ? checkIn.getEmotionalState().getDisplayName() : null)
                .createdAt(checkIn.getCreatedAt())
                .updatedAt(checkIn.getUpdatedAt())
                .tasks(taskDetails)
                .build();
    }
    private String normalizeComment(String comment) {
        if (comment == null) return null;
        String trimmed = comment.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void validateTasksBelongToActivePlan(Long estudianteId, List<TaskCheckInRequest> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        HabitPlan activePlan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new BusinessException(
                        "No tienes un plan de acompañamiento activo. "
                                + "Tu psicoorientador debe crearlo para que puedas informar tu cumplimiento."));

        java.util.Set<Long> planTaskIds = habitTaskRepository.findByHabitPlanId(activePlan.getId())
                .stream()
                .map(HabitTask::getId)
                .collect(java.util.stream.Collectors.toSet());

        for (TaskCheckInRequest taskRequest : tasks) {
            if (!planTaskIds.contains(taskRequest.getTaskId())) {
                throw new BusinessException("Una de las actividades no pertenece a tu plan activo.");
            }
        }
    }


}