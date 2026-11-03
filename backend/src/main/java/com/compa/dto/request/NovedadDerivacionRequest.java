package com.compa.dto.request;

import com.compa.enums.EstadoDerivacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class NovedadDerivacionRequest {

    // Opcional: si no se envia se usa la fecha de hoy
    private LocalDate fecha;

    @NotBlank(message = "La descripcion es obligatoria")
    @Size(max = 5000, message = "La descripcion no puede superar los 5000 caracteres")
    private String descripcion;

    @NotNull(message = "El estado es obligatorio")
    private EstadoDerivacion estado;
}