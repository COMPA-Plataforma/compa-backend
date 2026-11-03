package com.compa.dto.response;

import com.compa.enums.DestinoDerivacion;
import com.compa.enums.EstadoDerivacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

// Derivacion con su historial de novedades (vista del psicoorientador)
@Data
@Builder
public class DerivacionHistorialResponse {
    private Long id;
    private Long estudianteId;
    private String estudianteNombre;
    private DestinoDerivacion destino;
    private String destinoNombre;
    private LocalDate fecha;
    private String motivo;
    private EstadoDerivacion estado;
    private String estadoEtiqueta;
    // Dias desde la fecha de la derivacion hasta hoy
    private long diasTranscurridos;
    private String orientadorNombre;
    // En orden cronologico
    private List<NovedadDerivacionResponse> novedades;
}