package com.compa.dto.request;

import com.compa.enums.DecisionRevision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RevisionAutorizacionRequest {

    @NotNull(message = "Indica si apruebas o rechazas el documento")
    private DecisionRevision decision;

    // Obligatorio al rechazar: el estudiante lo ve para saber qué corregir
    @Size(max = 500, message = "El motivo no puede superar los 500 caracteres")
    private String motivo;
}