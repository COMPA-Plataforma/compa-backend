package com.compa.dto.request;

import com.compa.enums.DestinoDerivacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DerivacionRequest {

    @NotNull(message = "El destino es obligatorio")
    private DestinoDerivacion destino;

    // Obligatorio solo cuando el destino es OTRA (se valida en el servicio)
    @Size(max = 200, message = "El nombre de la ruta no puede superar los 200 caracteres")
    private String destinoOtro;

    // Opcional: si no se envia se usa la fecha de hoy
    private LocalDate fecha;

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 5000, message = "El motivo no puede superar los 5000 caracteres")
    private String motivo;
}