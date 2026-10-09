package com.compa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AtencionVersionResponse {
    private int numero;
    private LocalDateTime fecha;
    private Long autorId;
    private String autorNombre;
    private String anamnesis;
    private List<AtencionResponse.MotivoItem> motivos;
    private String motivoOtro;
    private String impresionDiagnostica;
}