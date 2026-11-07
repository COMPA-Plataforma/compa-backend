package com.compa.dto.response;


import com.compa.enums.FrecuenciaRecordatorio;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Builder
public class ConfiguracionRecordatorioResponse {
    private String hora;                       // "20:00"
    private FrecuenciaRecordatorio frecuencia;
    private List<DayOfWeek> dias;              // vacía si es diaria
    private boolean predeterminada;            // true si no la ha personalizado
    private String horaPredeterminada;         // la del sistema
}