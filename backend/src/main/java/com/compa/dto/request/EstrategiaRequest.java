package com.compa.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EstrategiaRequest {

    @NotBlank(message = "La descripción de la estrategia es obligatoria")
    @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de la sesión es obligatoria")
    @PastOrPresent(message = "La fecha de la sesión no puede ser futura")
    private LocalDate fechaSesion;
}