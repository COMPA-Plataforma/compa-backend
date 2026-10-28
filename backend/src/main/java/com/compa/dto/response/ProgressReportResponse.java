package com.compa.dto.response;

import com.compa.enums.EmotionalState;
import com.compa.enums.HealthStatus;
import com.compa.enums.PlanStatus;
import com.compa.enums.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class ProgressReportResponse
{
    // Encabezado
    private String estudianteFullName;
    private String orientadorFullName;
    private LocalDateTime generatedAt;

    // Métricas de adherencia
    private Double weeklyCompliance;
    private Integer currentStreak;
    private Integer bestStreak;

    // Niveles de riesgo
    private List<RiskDayDTO> last7DaysRisk;
    private Long highRiskCount;

    // Evolución de salud
    private HealthStatus initialHealthStatus;
    private HealthStatus currentHealthStatus;
    private LocalDateTime initialStatusDate;
    private LocalDateTime currentStatusDate;

    // Planes de hábitos
    private List<HabitPlanDTO> habitPlans;

    // Conclusiones
    private List<ConclusionDTO> conclusions;

    // Adherencia al plan en un periodo: % informado, % validado y estado de ánimo
    private AdherenciaDTO adherencia;

    // DTOs internos
    @Data
    @Builder
    public static class RiskDayDTO {
        private LocalDate date;
        private RiskLevel riskLevel;
        private Double compliancePercentage;
    }

    @Data
    @Builder
    public static class HabitPlanDTO {
        private String name;
        private LocalDate startDate;
        private PlanStatus status;
    }

    @Data
    @Builder
    public static class ConclusionDTO {
        private Long id;
        private String content;
        private String orientadorName;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class AdherenciaDTO {
        // Periodo evaluado (si no se envía filtro: desde el inicio del plan hasta hoy)
        private LocalDate from;
        private LocalDate to;
        // false si el estudiante no tiene un plan activo
        private boolean hasActivePlan;
        // false si en el periodo no hay actividades informadas ni estados de ánimo
        private boolean hasInfo;
        // Actividades programadas en el periodo
        private int scheduled;
        // Informadas como cumplidas por el estudiante, y su % sobre lo programado
        private int reportedCompleted;
        private Double reportedPercentage;
        // Validadas por el psicoorientador, y su % sobre lo programado
        private int validated;
        private Double validatedPercentage;
        // Informadas como cumplidas que aún no se han revisado, y marcadas como no realizadas
        private int toValidate;
        private int notPerformed;
        // Estado de ánimo registrado en el periodo
        private int moodCheckIns;
        private EmotionalState predominantMood;
        private String predominantMoodLabel;
        private String predominantMoodIcon;
        private List<MoodCountDTO> mood;
    }

    @Data
    @Builder
    public static class MoodCountDTO {
        private EmotionalState state;
        private String label;
        private String icon;
        private int count;
    }
}