package com.compa.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ValidacionesResponse {
    // Fecha de la ultima sesion registrada (seguimiento no anulado); null si aun no hay ninguna
    private LocalDate ultimaSesion;
    // Desde que dia se listan los registros (la ultima sesion, o el filtro enviado)
    private LocalDate desde;
    private List<ActividadInformadaResponse> actividades;
}