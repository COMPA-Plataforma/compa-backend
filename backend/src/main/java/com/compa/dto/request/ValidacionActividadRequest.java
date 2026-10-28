package com.compa.dto.request;

import com.compa.enums.EstadoValidacion;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidacionActividadRequest {

    @NotNull(message = "Indica si la actividad queda validada o no realizada")
    private EstadoValidacion estado;
}