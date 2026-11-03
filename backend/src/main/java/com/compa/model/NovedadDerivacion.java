package com.compa.model;

import com.compa.enums.EstadoDerivacion;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Novedad de una derivacion: registro del avance del proceso (fecha, descripcion y
 * estado resultante). Las novedades no se editan ni se eliminan: forman el historial.
 */
@Entity
@Table(name = "novedades_derivacion")
@Getter
@Setter
@NoArgsConstructor
public class NovedadDerivacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "derivacion_id", nullable = false)
    private Derivacion derivacion;

    // Psicoorientador que registro la novedad
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador orientador;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Estado en que queda la derivacion despues de esta novedad
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoDerivacion estado;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}