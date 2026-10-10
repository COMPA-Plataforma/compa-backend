package com.compa.dto.request;

import com.compa.enums.TaskPriority;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class HabitTaskRequest {
    @NotBlank(message = "El nombre de la tarea es obligatorio")
    private String name;
    @NotBlank(message = "La descripcion de la actividad es obligatoria")
    private String description;
    private TaskPriority priority = TaskPriority.MEDIA;
    private boolean mandatory = false;
    // Frecuencia (opcional): veces por semana y/o días específicos
    @Min(value = 1, message = "El objetivo semanal debe ser de al menos 1 vez")
    @Max(value = 7, message = "El objetivo semanal no puede superar 7 veces")
    private Integer weeklyGoal;
    private List<String> specificDays = new ArrayList<>();
    // Fecha límite (opcional)
    private LocalDate dueDate;
    // Fecha de la sesión en la que se acordó. Solo se usa al agregar una actividad en una
    // sesión posterior; si no se envía se toma la fecha de hoy.
    private LocalDate sessionDate;
}