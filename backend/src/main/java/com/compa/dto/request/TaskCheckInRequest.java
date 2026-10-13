package com.compa.dto.request;

import com.compa.enums.TaskBarrier;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TaskCheckInRequest {
    @NotNull(message = "El id de la tarea es obligatorio")
    private Long taskId;

    @NotNull(message = "El estado de completado es obligatorio")
    private boolean completed;

    private TaskBarrier barrier;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comment;

}
