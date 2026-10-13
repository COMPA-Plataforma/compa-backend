package com.compa.model;

import com.compa.enums.MotivoConsulta;
import com.compa.enums.PrioridadMensaje;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mensaje del banco de acompañamiento semanal. Si "motivo" es null, es un mensaje
 * general (para estudiantes sin motivos de consulta registrados).
 */
@Entity
@Table(name = "mensajes_acompanamiento")
@Getter
@Setter
@NoArgsConstructor
public class MensajeAcompanamiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MotivoConsulta motivo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadMensaje prioridad;

    // Un mensaje inactivo ya no se envía, pero se conserva en el historial de envíos
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean activo = true;

    public MensajeAcompanamiento(MotivoConsulta motivo, PrioridadMensaje prioridad, String texto) {
        this.motivo = motivo;
        this.prioridad = prioridad;
        this.texto = texto;
    }
}