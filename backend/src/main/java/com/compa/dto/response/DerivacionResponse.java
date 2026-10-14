package com.compa.dto.response;

import com.compa.enums.AlertStatus;
import com.compa.enums.DestinoDerivacion;
import com.compa.enums.EstadoDerivacion;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DerivacionResponse {
    private Long id;
    private Long estudianteId;
    private Long alertId;
    private AlertStatus alertStatus;
    private DestinoDerivacion destino;
    // Texto listo para mostrar: "EPS", "Hospital Alma Mater" o el nombre de la otra ruta
    private String destinoNombre;
    private LocalDate fecha;
    private String motivo;
    private EstadoDerivacion estado;
    private Long orientadorId;
    private String orientadorNombre;
    private LocalDateTime createdAt;
}