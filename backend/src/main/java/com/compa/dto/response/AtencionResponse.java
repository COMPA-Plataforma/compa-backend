package com.compa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AtencionResponse {
    private Long id;
    private Long estudianteId;
    private Long orientadorId;
    private String orientadorNombre;
    private String entrevistaRecepcion;
    private String situacionActual;
    private String antecedentes;
    private String aspectosPsicosociales;
    private String relacionesFamiliares;
    private String observacionesSalud;
    private Boolean afiliadoEps;
    private String epsNombre;
    private List<MotivoItem> motivos;
    private String motivoOtro;
    private String impresionDiagnostica;
    private int version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public record MotivoItem(String codigo, String nombre) {
    }
}