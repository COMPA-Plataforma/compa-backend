package com.compa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class HabitPlanRequest {
    @NotBlank(message = "El nombre del plan es obligatorio")
    private String name;
    private String description;
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate startDate;
    private LocalDate endDate;
    // Fecha de la sesión en la que se acordó el plan. Si no se envía se toma la fecha de hoy.
    private LocalDate sessionDate;
    // Actividades acordadas en la sesión. Al crear el plan debe haber al menos una
    // (se valida en el servicio, para no afectar la edición del plan).
    @Valid
    private List<HabitTaskRequest> tasks = new ArrayList<>();
}