package com.compa.dto.response;

import com.compa.enums.EstadoAutorizacionMenor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

// Estado de la autorización del representante legal de un estudiante menor de edad.
@Getter
@Builder
public class AutorizacionMenorResponse {
    private Long estudianteId;
    private String estudianteNombre;
    private EstadoAutorizacionMenor estado;
    private String versionDocumento;

    // Datos del acudiente que registró el psicoorientador (null si aún no los ha registrado).
    // Con ellos se llena el documento que el estudiante descarga.
    private GuardianResponse acudiente;

    // El estudiante puede subir el documento: está pendiente o fue rechazado, y ya hay acudiente
    private boolean puedeSubir;

    private String archivoNombre;
    private String archivoTipo;
    private Long archivoTamano;
    private LocalDateTime subidoEn;

    private String revisadoPorNombre;
    private LocalDateTime revisadoEn;
    private String motivoRechazo;
}