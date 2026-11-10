package com.compa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Informe agregado y anonimizado: nunca incluye datos de estudiantes individuales.
@Data
@Builder
public class InformeMotivosResponse {

    private LocalDate desde;
    private LocalDate hasta;
    // Codigo del motivo si se filtro por uno; null si se consultaron todos
    private String motivoFiltro;

    // Solo motivos con al menos 5 estudiantes, del mas consultado al menos consultado
    private List<MotivoItem> motivos;

    // true si se omitieron motivos con menos de 5 estudiantes (sin decir cuantos ni cuales)
    private boolean hayMotivosOmitidos;

    // Sugerencia de taller para el motivo principal; null si no hay motivos visibles
    private Sugerencia sugerencia;

    @Data
    @Builder
    public static class MotivoItem {
        private String motivo;
        private String motivoNombre;
        private long estudiantes;
        // Promedio del cumplimiento mensual (%); null si ningun estudiante tiene adherencia calculada
        private Double adherenciaPromedio;
    }

    @Data
    @Builder
    public static class Sugerencia {
        private String motivo;
        private String motivoNombre;
        private String enfoque;
        private List<String> talleres;
        private String nota;
    }
}