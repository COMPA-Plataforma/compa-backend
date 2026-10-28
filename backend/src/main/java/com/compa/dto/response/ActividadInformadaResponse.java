package com.compa.dto.response;

import com.compa.enums.EstadoValidacion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Una actividad que el estudiante informo en un check-in, con su estado de validacion.
@Getter
@Builder
public class ActividadInformadaResponse {
    // id del registro (TaskCheckIn): es el que se envia para validar
    private Long id;
    private Long actividadId;
    private String actividadNombre;
    // Dia del check-in
    private LocalDate fecha;
    // Lo que informo el estudiante
    private boolean informadaCumplida;
    private String barrera;
    // Nombre legible de la barrera (para mostrar en pantalla)
    private String barreraEtiqueta;
    // Comentario que dejo el estudiante al informar la actividad
    private String comentario;
    // Null mientras no se ha validado
    private EstadoValidacion estadoValidacion;
    private LocalDateTime validadaEn;
    private String validadaPorNombre;
}