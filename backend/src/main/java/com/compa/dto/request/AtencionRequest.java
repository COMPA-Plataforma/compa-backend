package com.compa.dto.request;

import com.compa.enums.MotivoConsulta;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class AtencionRequest {

    @Size(max = 5000, message = "La anamnesis no puede superar los 5000 caracteres")
    private String anamnesis;

    private Set<MotivoConsulta> motivos;

    @Size(max = 200, message = "El campo Otro no puede superar los 200 caracteres")
    private String motivoOtro;

    @Size(max = 300, message = "La impresion diagnostica no puede superar los 300 caracteres")
    private String impresionDiagnostica;
}
