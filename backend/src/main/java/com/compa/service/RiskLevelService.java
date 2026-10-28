package com.compa.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.compa.enums.AlertType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.compa.dto.response.RiskLevelHistoryResponse;
import com.compa.dto.response.RiskLevelResponse;
import com.compa.enums.EstudianteStatus;
import com.compa.enums.PlanStatus;
import com.compa.enums.RiskLevel;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.DailyCheckIn;
import com.compa.model.Estudiante;
import com.compa.model.RiskLevelHistory;
import com.compa.model.TaskCheckIn;
import com.compa.repository.DailyCheckInRepository;
import com.compa.repository.HabitPlanRepository;
import com.compa.repository.HabitTaskRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.RiskLevelHistoryRepository;
import com.compa.repository.TaskCheckInRepository;
import org.springframework.context.annotation.Lazy;


@Service
public class RiskLevelService {
    private final EstudianteRepository estudianteRepository;
    private final HabitPlanRepository habitPlanRepository;
    private final HabitTaskRepository habitTaskRepository;
    private final DailyCheckInRepository checkInRepository;
    private final TaskCheckInRepository taskCheckInRepository;
    private final RiskLevelHistoryRepository riskLevelHistoryRepository;
    private final AlertService alertService;

    public RiskLevelService(EstudianteRepository estudianteRepository,
                            HabitPlanRepository habitPlanRepository,
                            HabitTaskRepository habitTaskRepository,
                            DailyCheckInRepository checkInRepository,
                            TaskCheckInRepository taskCheckInRepository,
                            RiskLevelHistoryRepository riskLevelHistoryRepository,
                            @Lazy AlertService alertService) {    // ← agregar @Lazy aquí
        this.estudianteRepository = estudianteRepository;
        this.habitPlanRepository = habitPlanRepository;
        this.habitTaskRepository = habitTaskRepository;
        this.checkInRepository = checkInRepository;
        this.taskCheckInRepository = taskCheckInRepository;
        this.riskLevelHistoryRepository = riskLevelHistoryRepository;
        this.alertService = alertService;
    }

    // Calcular y guardar nivel de riesgo de un estudiante específico
    @Transactional
    public RiskLevelResponse evaluateEstudiante(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        double compliance = calculateWeeklyCompliance(estudianteId);
        RiskLevel riskLevel = determineRiskLevel(compliance);

        RiskLevel previousRiskLevel = riskLevelHistoryRepository
                .findTopByEstudianteIdOrderByEvaluatedDateDesc(estudianteId)
                .map(RiskLevelHistory::getRiskLevel)
                .orElse(null);

        LocalDate today = LocalDate.now();

        // Si ya fue evaluado hoy, actualiza el registro
        RiskLevelHistory history = riskLevelHistoryRepository
                .findByEstudianteIdAndEvaluatedDate(estudianteId, today)
                .orElse(new RiskLevelHistory());

        history.setEstudiante(estudiante);
        history.setRiskLevel(riskLevel);
        history.setPreviousRiskLevel(previousRiskLevel);
        history.setCompliancePercentage(compliance);
        history.setEvaluatedDate(today);
        riskLevelHistoryRepository.save(history);

        // Generar alerta si el nivel es ROJO o AMARILLO
        if (riskLevel == RiskLevel.ROJO) {
            alertService.createAlertIfNotExists(
                    estudianteId,
                    AlertType.RIESGO_ALTO,
                    "El estudiante " + estudiante.getName() + " " + estudiante.getLastName() +
                            " tiene un cumplimiento del " + compliance + "% esta semana."
            );
        } else if (riskLevel == RiskLevel.AMARILLO) {
            alertService.createAlertIfNotExists(
                    estudianteId,
                    AlertType.RIESGO_MEDIO,
                    "El estudiante " + estudiante.getName() + " " + estudiante.getLastName() +
                            " tiene un cumplimiento del " + compliance + "% esta semana."
            );
        }

        return toResponse(estudiante, history);
    }

    // Obtener nivel de riesgo actual de un estudiante
    @Transactional(readOnly = true)
    public RiskLevelResponse getCurrentRiskLevel(Long estudianteId) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        return riskLevelHistoryRepository
                .findTopByEstudianteIdOrderByEvaluatedDateDesc(estudianteId)
                .map(h -> toResponse(estudiante, h))
                .orElseGet(() -> {
                    // Si no hay historial, calcula en tiempo real sin guardar
                    double compliance = calculateWeeklyCompliance(estudianteId);
                    RiskLevel riskLevel = determineRiskLevel(compliance);
                    RiskLevelHistory temp = new RiskLevelHistory();
                    temp.setRiskLevel(riskLevel);
                    temp.setCompliancePercentage(compliance);
                    temp.setEvaluatedDate(LocalDate.now(java.time.ZoneId.of("America/Bogota")));
                    return toResponse(estudiante, temp);
                });
    }

    // Obtener historial de niveles de riesgo de un estudiante
    @Transactional(readOnly = true)
    public List<RiskLevelHistoryResponse> getRiskLevelHistory(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        return riskLevelHistoryRepository
                .findByEstudianteIdOrderByEvaluatedDateDesc(estudianteId)
                .stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());
    }

    // Evaluar todos los estudiantes activos (para ejecutar diariamente)
    @Transactional
    public List<RiskLevelResponse> evaluateAllActiveEstudiantes() {
        return evaluar(estudianteRepository.findByStatus(EstudianteStatus.ACTIVO));
    }

    // Disparo manual del psicoorientador: solo evalua a sus estudiantes
    @Transactional
    public List<RiskLevelResponse> evaluateAllActiveEstudiantes(String orientadorEmail) {
        return evaluar(estudianteRepository.findByStatusAndOrientadorEmail(EstudianteStatus.ACTIVO, orientadorEmail));
    }

    private List<RiskLevelResponse> evaluar(List<Estudiante> activeEstudiantes) {
        List<RiskLevelResponse> results = new ArrayList<>();
        for (Estudiante estudiante : activeEstudiantes) {
            results.add(evaluateEstudiante(estudiante.getId()));
        }
        return results;
    }

    // Obtener todos los estudiantes activos con su nivel de riesgo actual
    @Transactional(readOnly = true)
    public List<RiskLevelResponse> getAllEstudiantesRiskLevel(String orientadorEmail) {
        List<Estudiante> activeEstudiantes =
                estudianteRepository.findByStatusAndOrientadorEmail(EstudianteStatus.ACTIVO, orientadorEmail);
        return activeEstudiantes.stream()
                .map(p -> getCurrentRiskLevel(p.getId()))
                .collect(Collectors.toList());
    }

    // --- Lógica de cálculo ---

    private double calculateWeeklyCompliance(Long estudianteId) {
        // Obtener plan activo
        var activePlan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElse(null);

        if (activePlan == null)
            return 0.0;

        // Tareas del plan
        int totalTasks = habitTaskRepository.findByHabitPlanId(activePlan.getId()).size();
        if (totalTasks == 0)
            return 0.0;

        // Check-ins de los últimos 7 días
        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/Bogota"));
        LocalDate weekAgo = today.minusDays(6);

        List<DailyCheckIn> checkIns = checkInRepository
                .findByEstudianteIdOrderByCheckInDateDesc(estudianteId)
                .stream()
                .filter(c -> !c.getCheckInDate().isBefore(weekAgo) && !c.getCheckInDate().isAfter(today))
                .collect(Collectors.toList());

        if (checkIns.isEmpty())
            return 0.0;

        // Total de tareas completadas en la semana
        int totalExpected = totalTasks * 7;
        int totalCompleted = 0;

        for (DailyCheckIn checkIn : checkIns) {
            List<TaskCheckIn> taskCheckIns = taskCheckInRepository.findByCheckInId(checkIn.getId());
            totalCompleted += taskCheckIns.stream().filter(TaskCheckIn::isCompleted).count();
        }

        return Math.round((totalCompleted * 100.0 / totalExpected) * 10.0) / 10.0;
    }

    private RiskLevel determineRiskLevel(double compliance) {
        if (compliance >= 80)
            return RiskLevel.VERDE;
        if (compliance >= 50)
            return RiskLevel.AMARILLO;
        return RiskLevel.ROJO;
    }

    // --- Mappers ---

    private RiskLevelResponse toResponse(Estudiante estudiante, RiskLevelHistory history) {
        return RiskLevelResponse.builder()
                .estudianteId(estudiante.getId())
                .estudianteName(estudiante.getName() + " " + estudiante.getLastName())
                .riskLevel(history.getRiskLevel())
                .riskLevelDisplay(history.getRiskLevel().getDisplayName())
                .riskLevelDescription(history.getRiskLevel().getDescription())
                .compliancePercentage(history.getCompliancePercentage())
                .evaluatedDate(history.getEvaluatedDate())
                .createdAt(history.getCreatedAt())
                .build();
    }

    private RiskLevelHistoryResponse toHistoryResponse(RiskLevelHistory h) {
        return RiskLevelHistoryResponse.builder()
                .id(h.getId())
                .riskLevel(h.getRiskLevel())
                .riskLevelDisplay(h.getRiskLevel().getDisplayName())
                .previousRiskLevel(h.getPreviousRiskLevel())
                .previousRiskLevelDisplay(h.getPreviousRiskLevel() != null
                        ? h.getPreviousRiskLevel().getDisplayName()
                        : null)
                .compliancePercentage(h.getCompliancePercentage())
                .evaluatedDate(h.getEvaluatedDate())
                .createdAt(h.getCreatedAt())
                .build();
    }

}