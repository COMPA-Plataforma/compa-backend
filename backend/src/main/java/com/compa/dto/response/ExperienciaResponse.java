package com.compa.dto.response;

import lombok.Builder;
import lombok.Getter;
 
import java.time.LocalDate;
import java.time.LocalDateTime;
 
@Getter
@Builder
public class ExperienciaResponse {
    private Long id;
    private Long actividadId;
    private String actividadNombre;
    private String texto;
    private LocalDate fecha;
    private LocalDateTime createdAt;
    // Fecha de la ultima correccion; null si nunca se corrigio
    private LocalDateTime updatedAt;
    // true si todavia se puede corregir (solo el mismo dia en que se registro)
    private boolean editable;
}
 
