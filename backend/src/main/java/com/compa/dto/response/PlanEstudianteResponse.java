package com.compa.dto.response;

import com.compa.enums.PlanStatus;
import com.compa.enums.TaskPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Vista del plan de acompañamiento para el estudiante (solo lectura).
// No incluye anamnesis, impresión diagnóstica ni ningún dato clínico.
@Getter
@Builder
public class PlanEstudianteResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;          // fecha límite del plan (puede ser null)
    private PlanStatus status;
    private LocalDateTime createdAt;    // fecha en que se acordó el plan
    private List<Actividad> tasks;

    @Getter
    @Builder
    public static class Actividad {
        private Long id;
        private String name;
        private String description;
        private TaskPriority priority;
        private boolean mandatory;
        private Integer weeklyGoal;           // veces por semana (puede ser null)
        private List<String> specificDays;    // días específicos (puede estar vacía)
        private LocalDateTime createdAt;      // fecha en que se acordó la actividad
    }
}