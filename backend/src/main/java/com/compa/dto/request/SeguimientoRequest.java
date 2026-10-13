package com.compa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SeguimientoRequest {

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 5000, message = "La descripcion no puede superar los 5000 caracteres")
    private String descripcion;

    // Marcas de la sesion. Si no se envian quedan en falso.
    private boolean faltoCita;
    private boolean faltoPrimeraCita;
    private boolean canceladaEstudiante;
    private boolean canceladaDepartamento;
    private boolean citaVirtual;
    private boolean primeraVez;
    private boolean cierreProceso;
    private boolean remision;
}