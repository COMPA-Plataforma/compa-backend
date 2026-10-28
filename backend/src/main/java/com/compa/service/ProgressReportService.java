package com.compa.service;

import com.compa.dto.request.ConclusionRequest;
import com.compa.dto.response.ProgresoPlanResponse;
import com.compa.dto.response.ProgressReportResponse;
import com.compa.enums.EmotionalState;
import com.compa.enums.PlanStatus;
import com.compa.enums.RiskLevel;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.*;
import com.compa.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressReportService {

    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;
    private final AdherenceSnapshotRepository adherenceSnapshotRepository;
    private final RiskLevelHistoryRepository riskLevelHistoryRepository;
    private final HealthStatusHistoryRepository healthStatusHistoryRepository;
    private final HabitPlanRepository habitPlanRepository;
    private final OrientadorNotaRepository conclusionRepository;
    private final DailyCheckInRepository dailyCheckInRepository;
    private final ProgresoEstudianteService progresoEstudianteService;

    // Informe de adherencia y progreso. Lo consulta el psicoorientador asignado al estudiante, o el propio
    // estudiante (su pantalla "Mi progreso"). from/to filtran la adherencia y el estado de ánimo (opcionales).
    public ProgressReportResponse getProgressReport(Long estudianteId, String email,
                                                    LocalDate from, LocalDate to) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        Orientador orientador = orientadorRepository.findByEmail(email).orElse(null);
        if (orientador != null) {
            if (estudiante.getOrientador() == null
                    || !estudiante.getOrientador().getId().equals(orientador.getId())) {
                throw new ForbiddenException(
                        "Solo el psicoorientador asignado puede consultar el informe de este estudiante.");
            }
        } else {
            Estudiante propio = estudianteRepository.findByEmail(email).orElse(null);
            if (propio == null || !propio.getId().equals(estudianteId)) {
                throw new ForbiddenException("No tienes acceso al informe de este estudiante.");
            }
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la final.");
        }
        return construir(estudianteId, from, to);
    }

    public ProgressReportResponse getProgressReport(Long estudianteId) {
        return construir(estudianteId, null, null);
    }

    private ProgressReportResponse construir(Long estudianteId, LocalDate from, LocalDate to) {

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        // 1. Métricas de adherencia
        AdherenceSnapshot latestSnapshot = adherenceSnapshotRepository
                .findByEstudianteOrderBySnapshotDateDesc(estudiante)
                .stream().findFirst().orElse(null);

        Double weeklyCompliance = latestSnapshot != null ? latestSnapshot.getWeeklyCompliance() : 0.0;
        Integer currentStreak = latestSnapshot != null ? latestSnapshot.getCurrentStreak() : 0;

        // Mejor racha histórica
        Integer bestStreak = adherenceSnapshotRepository
                .findByEstudianteOrderBySnapshotDateDesc(estudiante)
                .stream()
                .mapToInt(AdherenceSnapshot::getCurrentStreak)
                .max()
                .orElse(0);

        // 2. Últimos 7 días de riesgo
        List<RiskLevelHistory> riskHistory = riskLevelHistoryRepository
                .findTop7ByEstudianteOrderByEvaluatedDateDesc(estudiante);

        List<ProgressReportResponse.RiskDayDTO> last7DaysRisk = riskHistory.stream()
                .map(r -> ProgressReportResponse.RiskDayDTO.builder()
                        .date(r.getEvaluatedDate())
                        .riskLevel(r.getRiskLevel())
                        .compliancePercentage(r.getCompliancePercentage())
                        .build())
                .collect(Collectors.toList());

        // Veces en riesgo ROJO
        Long highRiskCount = riskLevelHistoryRepository
                .countByEstudianteAndRiskLevel(estudiante, RiskLevel.ROJO);

        // 3. Evolución de salud
        List<HealthStatusHistory> statusHistory = healthStatusHistoryRepository
                .findByClinicalInfo_EstudianteOrderByChangedAtAsc(estudiante);

        HealthStatusHistory initialStatus = statusHistory.isEmpty() ? null : statusHistory.get(0);
        HealthStatusHistory currentStatus = statusHistory.isEmpty() ? null
                : statusHistory.get(statusHistory.size() - 1);

        // 4. Planes de hábitos
        List<ProgressReportResponse.HabitPlanDTO> habitPlans = habitPlanRepository
                .findByEstudiante(estudiante)
                .stream()
                .map(p -> ProgressReportResponse.HabitPlanDTO.builder()
                        .name(p.getName())
                        .startDate(p.getStartDate())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());

        // 5. Conclusiones
        List<ProgressReportResponse.ConclusionDTO> conclusions = conclusionRepository
                .findByEstudianteIdOrderByCreatedAtDesc(estudianteId)
                .stream()
                .map(c -> ProgressReportResponse.ConclusionDTO.builder()
                        .id(c.getId())
                        .content(c.getContent())
                        .orientadorName(c.getOrientador().getName()
                                + " " + c.getOrientador().getLastName())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ProgressReportResponse.builder()
                .estudianteFullName(estudiante.getName() + " " + estudiante.getLastName())
                .orientadorFullName("COMPA - Acompañamiento estudiantil")
                .generatedAt(LocalDateTime.now())
                .weeklyCompliance(weeklyCompliance)
                .currentStreak(currentStreak)
                .bestStreak(bestStreak)
                .last7DaysRisk(last7DaysRisk)
                .highRiskCount(highRiskCount)
                .initialHealthStatus(initialStatus != null ? initialStatus.getNewStatus() : null)
                .currentHealthStatus(currentStatus != null ? currentStatus.getNewStatus() : null)
                .initialStatusDate(initialStatus != null ? initialStatus.getChangedAt() : null)
                .currentStatusDate(currentStatus != null ? currentStatus.getChangedAt() : null)
                .habitPlans(habitPlans)
                .conclusions(conclusions)
                .adherencia(construirAdherencia(estudianteId, from, to))
                .build();
    }

    // % informado por el estudiante, % validado por el psicoorientador y resumen del estado de ánimo
    private ProgressReportResponse.AdherenciaDTO construirAdherencia(Long estudianteId, LocalDate from, LocalDate to) {
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        boolean hasPlan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO).isPresent();

        ProgresoPlanResponse progreso = hasPlan
                ? progresoEstudianteService.getProgreso(estudianteId, from, to)
                : null;

        // Periodo: el del plan activo; sin plan, el filtro o los últimos 30 días
        LocalDate desde = progreso != null ? progreso.getFrom() : (from != null ? from : hoy.minusDays(29));
        LocalDate hasta = progreso != null ? progreso.getTo() : (to != null ? to : hoy);

        // Estado de ánimo de los check-ins del periodo
        Map<EmotionalState, Integer> conteo = new EnumMap<>(EmotionalState.class);
        for (DailyCheckIn c : dailyCheckInRepository.findByEstudianteIdOrderByCheckInDateDesc(estudianteId)) {
            LocalDate d = c.getCheckInDate();
            if (c.getEmotionalState() != null && !d.isBefore(desde) && !d.isAfter(hasta)) {
                conteo.merge(c.getEmotionalState(), 1, Integer::sum);
            }
        }
        List<ProgressReportResponse.MoodCountDTO> mood = new ArrayList<>();
        for (EmotionalState e : EmotionalState.values()) {
            int n = conteo.getOrDefault(e, 0);
            if (n > 0) {
                mood.add(ProgressReportResponse.MoodCountDTO.builder()
                        .state(e).label(e.getDisplayName()).icon(e.getIcon()).count(n).build());
            }
        }
        int moodCheckIns = conteo.values().stream().mapToInt(Integer::intValue).sum();
        EmotionalState predominante = mood.stream()
                .max(Comparator.comparingInt(ProgressReportResponse.MoodCountDTO::getCount))
                .map(ProgressReportResponse.MoodCountDTO::getState).orElse(null);

        int programadas = progreso != null ? progreso.getScheduled() : 0;
        int informadas = progreso != null ? progreso.getCompleted() : 0;
        int validadas = progreso != null ? progreso.getValidated() : 0;

        return ProgressReportResponse.AdherenciaDTO.builder()
                .from(desde)
                .to(hasta)
                .hasActivePlan(hasPlan)
                .hasInfo((progreso != null && progreso.isHasReports()) || moodCheckIns > 0)
                .scheduled(programadas)
                .reportedCompleted(informadas)
                .reportedPercentage(porcentaje(informadas, programadas))
                .validated(validadas)
                .validatedPercentage(porcentaje(validadas, programadas))
                .toValidate(progreso != null ? progreso.getToValidate() : 0)
                .notPerformed(progreso != null ? progreso.getNotPerformed() : 0)
                .moodCheckIns(moodCheckIns)
                .predominantMood(predominante)
                .predominantMoodLabel(predominante != null ? predominante.getDisplayName() : null)
                .predominantMoodIcon(predominante != null ? predominante.getIcon() : null)
                .mood(mood)
                .build();
    }

    private static Double porcentaje(int parte, int total) {
        if (total == 0) return null;
        return Math.round(parte * 1000.0 / total) / 10.0;
    }

    public OrientadorNota addConclusion(Long estudianteId, ConclusionRequest request) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        Orientador orientador = orientadorRepository.findById(request.getOrientadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Orientador", request.getOrientadorId()));

        OrientadorNota conclusion = OrientadorNota.builder()
                .estudiante(estudiante)
                .orientador(orientador)
                .content(request.getContent())
                .build();

        return conclusionRepository.save(conclusion);
    }
}