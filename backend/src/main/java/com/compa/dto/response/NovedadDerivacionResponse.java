package com.compa.dto.response;

import com.compa.enums.EstadoDerivacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class NovedadDerivacionResponse {
    private Long id;
    private Long derivacionId;
    private LocalDate fecha;
    private String descripcion;
    private EstadoDerivacion estado;
    private String estadoEtiqueta;
    // Estado actual de la derivacion despues de registrar esta novedad
    private EstadoDerivacion estadoActual;
    // Dias transcurridos desde la fecha de la derivacion hasta hoy
    private long diasTranscurridos;
    private Long orientadorId;
    private String orientadorNombre;
    private LocalDateTime createdAt;
}