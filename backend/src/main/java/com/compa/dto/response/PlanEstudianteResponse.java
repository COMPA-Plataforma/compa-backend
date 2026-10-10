package com.compa.dto.response;

import com.compa.enums.PlanStatus;
import com.compa.enums.TaskPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Vista del plan de acompañamiento para el estudiante (solo lectura).
// Solo expone lo que el estudiante debe ver: no incluye anamnesis, impresión
// diagnóstica ni ningún dato clínico.
@Getter
@Builder
public class PlanEstudianteResponse {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    // Fecha límite del plan; puede ser null si no se definió.
    private LocalDate endDate;
    private PlanStatus status;
    // Fecha en que se registró el plan en el sistema.
    private LocalDateTime createdAt;
    // Fecha de la sesión en que se acordó el plan.
    private LocalDate agreedDate;
    private List<Actividad> tasks;

    @Getter
    @Builder
    public static class Actividad {
        private Long id;
        private String name;
        private String description;
        private TaskPriority priority;
        private boolean mandatory;
        // Frecuencia: veces por semana y/o días específicos (ambas pueden ser null/vacías).
        private Integer weeklyGoal;
        private List<String> specificDays;
        // Fecha límite de la actividad; puede ser null.
        private LocalDate dueDate;
        // Fecha de la sesión en que se acordó la actividad.
        private LocalDate agreedDate;
        // Fecha en que se registró la actividad en el sistema.
        private LocalDateTime createdAt;
    }
}