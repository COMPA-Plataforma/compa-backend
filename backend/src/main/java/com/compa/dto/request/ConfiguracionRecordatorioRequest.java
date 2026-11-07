package com.compa.dto.request;


import com.compa.enums.FrecuenciaRecordatorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.DayOfWeek;
import java.util.List;

@Data
public class ConfiguracionRecordatorioRequest {

    // Formato 24 horas, ej. "20:00"
    @NotBlank(message = "Elige la hora del recordatorio")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "La hora debe tener formato HH:mm")
    private String hora;

    @NotNull(message = "Elige la frecuencia")
    private FrecuenciaRecordatorio frecuencia;

    // Solo se usa con DIAS_ESPECIFICOS (ej. MONDAY, WEDNESDAY)
    private List<DayOfWeek> dias;
}