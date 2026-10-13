package com.compa.dto.request;

import com.compa.enums.EmotionalState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DailyCheckInRequest {

    @NotNull(message = "El estado emocional es obligatorio")
    private EmotionalState emotionalState;


    @Valid
    private List<TaskCheckInRequest> tasks = new ArrayList<>();
}