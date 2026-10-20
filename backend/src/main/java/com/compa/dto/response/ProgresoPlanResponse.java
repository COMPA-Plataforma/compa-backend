package com.compa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ProgresoPlanResponse {

    private String planName;
    private LocalDate from;
    private LocalDate to;

    // Totales del periodo
    private int scheduled;      // programadas
    private int completed;      // informadas como cumplidas
    private int notCompleted;   // informadas como no cumplidas
    private int pending;        // programadas sin informar

    // completed / scheduled * 100. Es null si no hay actividades programadas en el periodo
    private Double percentage;

    // false si el estudiante no ha informado ninguna actividad en el periodo
    private boolean hasReports;

    private List<ActividadProgreso> activities;

    @Data
    @Builder
    public static class ActividadProgreso {
        private Long taskId;
        private String name;
        private String frequency;   // texto legible: "Lunes y jueves", "3 veces por semana"...
        private int scheduled;
        private int completed;
        private int notCompleted;
        private int pending;
    }
}