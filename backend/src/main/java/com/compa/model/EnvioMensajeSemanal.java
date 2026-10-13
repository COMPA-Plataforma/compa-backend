package com.compa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de un mensaje semanal enviado a un estudiante. Hay uno por estudiante y por
 * semana (restricción única), y sirve para no repetir el mensaje de la semana anterior.
 */
@Entity
@Table(name = "envios_mensaje_semanal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"estudiante_id", "semana"}))
@Getter
@Setter
@NoArgsConstructor
public class EnvioMensajeSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mensaje_id", nullable = false)
    private MensajeAcompanamiento mensaje;

    // Lunes de la semana a la que corresponde el envío
    @Column(nullable = false)
    private LocalDate semana;

    @Column(name = "enviado_at", nullable = false)
    private LocalDateTime enviadoAt;
}