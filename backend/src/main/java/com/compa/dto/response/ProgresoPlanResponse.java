package com.compa.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// Progreso del estudiante en su plan de acompañamiento activo (solo lectura).
@Getter
@Builder
public class ProgresoPlanResponse {

    private String planName;
    private LocalDate from;
    private LocalDate to;
    private int scheduled;
    private int completed;
    private int notCompleted;
    private int pending;
    // completadas / programadas * 100. Es null si no hubo actividades programadas en el periodo.
    private Double percentage;
    // Validación del psicoorientador sobre lo que el estudiante informó
    private int validated;
    private int notPerformed;
    // Informadas como cumplidas que el psicoorientador todavía no ha revisado
    private int toValidate;
    // true si el estudiante informó al menos una actividad en el periodo
    private boolean hasReports;
    private List<ActividadProgreso> activities;

    @Getter
    @Builder
    public static class ActividadProgreso {
        private Long taskId;
        private String name;
        private String frequency;
        private int scheduled;
        private int completed;
        private int notCompleted;
        private int pending;
        private int validated;
        private int notPerformed;
        private int toValidate;
    }
}