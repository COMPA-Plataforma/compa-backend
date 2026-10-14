package com.compa.model;

import com.compa.enums.DestinoDerivacion;
import com.compa.enums.EstadoDerivacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Derivacion: remision de un estudiante a una ruta de atencion (EPS, Hospital Alma Mater u otra).
 * Nace siempre desde una alerta y queda vinculada a ella.
 */
@Entity
@Table(name = "derivaciones")
@Getter
@Setter
@NoArgsConstructor
public class Derivacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    // Psicoorientador que hizo la derivacion
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador orientador;

    // Alerta que origino la derivacion
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DestinoDerivacion destino;

    // Nombre de la ruta cuando el destino es OTRA
    @Column(name = "destino_otro", length = 200)
    private String destinoOtro;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDerivacion estado = EstadoDerivacion.DERIVADO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}