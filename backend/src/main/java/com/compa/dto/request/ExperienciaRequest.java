package com.compa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class ExperienciaRequest {
 
    // Obligatoria al registrar; al corregir se ignora (la actividad no cambia)
    private Long actividadId;
 
    @NotBlank(message = "La experiencia es obligatoria")
    @Size(max = 1000, message = "La experiencia no puede superar los 1000 caracteres")
    private String texto;
}
 