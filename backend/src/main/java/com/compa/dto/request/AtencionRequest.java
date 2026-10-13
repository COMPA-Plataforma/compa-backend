package com.compa.dto.request;

import com.compa.enums.MotivoConsulta;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * Al registrar, la entrevista de recepcion es obligatoria. Al actualizar, un campo omitido
 * (null) no cambia; un texto vacío ("") o una lista vacía ([]) lo borra
 * (excepto la entrevista de recepcion, que no puede quedar vacía).
 */
@Data
public class AtencionRequest {

    @Size(max = 5000, message = "La entrevista de recepcion no puede superar los 5000 caracteres")
    private String entrevistaRecepcion;

    @Size(max = 5000, message = "El campo situacion actual no puede superar los 5000 caracteres")
    private String situacionActual;

    @Size(max = 5000, message = "El campo antecedentes no puede superar los 5000 caracteres")
    private String antecedentes;

    @Size(max = 5000, message = "El campo aspectos psicosociales no puede superar los 5000 caracteres")
    private String aspectosPsicosociales;

    @Size(max = 5000, message = "El campo relaciones familiares no puede superar los 5000 caracteres")
    private String relacionesFamiliares;

    @Size(max = 5000, message = "El campo observaciones no puede superar los 5000 caracteres")
    private String observacionesSalud;

    private Set<MotivoConsulta> motivos;

    @Size(max = 200, message = "El campo Otro no puede superar los 200 caracteres")
    private String motivoOtro;

    @Size(max = 5000, message = "La impresion diagnostica no puede superar los 5000 caracteres")
    private String impresionDiagnostica;
}