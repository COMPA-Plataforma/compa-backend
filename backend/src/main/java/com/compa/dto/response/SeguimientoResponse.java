package com.compa.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SeguimientoResponse {
    private Long id;
    private Long estudianteId;
    private LocalDate fecha;
    private String descripcion;
    private Long autorId;
    private String autorNombre;
    private boolean faltoCita;
    private boolean faltoPrimeraCita;
    private boolean canceladaEstudiante;
    private boolean canceladaDepartamento;
    private boolean citaVirtual;
    private boolean primeraVez;
    private boolean cierreProceso;
    private boolean remision;
    // Un seguimiento anulado se conserva en el historial
    private boolean anulado;
    private LocalDateTime anuladoAt;
    private String anuladoPorNombre;
    private LocalDateTime createdAt;
}