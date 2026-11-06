package com.compa.dto.response;

import com.compa.enums.EstadoEstrategia;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class EstrategiaResponse {
    private Long id;
    private Long planId;
    private String planNombre;
    private String descripcion;
    private LocalDate fechaSesion;
    private EstadoEstrategia estado;
    private LocalDateTime creadaEn;
    // Fecha del último cambio; null si nunca se actualizó
    private LocalDateTime actualizadaEn;
    private LocalDateTime anuladaEn;
}
