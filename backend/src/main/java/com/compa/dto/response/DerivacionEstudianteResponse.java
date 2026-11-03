package com.compa.dto.response;

import com.compa.enums.EstadoDerivacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Vista del estudiante: solo destino, fechas y estados. Sin motivo ni descripciones de novedades.
@Data
@Builder
public class DerivacionEstudianteResponse {
    private Long id;
    private String destinoNombre;
    private LocalDate fecha;
    private EstadoDerivacion estado;
    private String estadoEtiqueta;
    private long diasTranscurridos;
    // En orden cronologico
    private List<Novedad> novedades;

    @Data
    @Builder
    public static class Novedad {
        private LocalDate fecha;
        private EstadoDerivacion estado;
        private String estadoEtiqueta;
    }
}