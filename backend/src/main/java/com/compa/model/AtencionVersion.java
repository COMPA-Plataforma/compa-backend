package com.compa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Copia de la atención en un momento dado. Se crea una por cada registro o
 * actualización, con fecha y autor, para no perder la información anterior.
 */
@Entity
@Table(name = "atencion_versiones")
@Getter
@Setter
@NoArgsConstructor
public class AtencionVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atencion_id", nullable = false)
    private Atencion atencion;

    @Column(nullable = false)
    private int numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador autor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "anamnesis", nullable = false, columnDefinition = "TEXT")
    private String entrevistaRecepcion;

    @Column(name = "situacion_actual", columnDefinition = "TEXT")
    private String situacionActual;

    @Column(name = "antecedentes", columnDefinition = "TEXT")
    private String antecedentes;

    @Column(name = "aspectos_psicosociales", columnDefinition = "TEXT")
    private String aspectosPsicosociales;

    @Column(name = "relaciones_familiares", columnDefinition = "TEXT")
    private String relacionesFamiliares;

    @Column(name = "observaciones_salud", columnDefinition = "TEXT")
    private String observacionesSalud;


    // Códigos de los motivos separados por coma
    @Column(columnDefinition = "TEXT")
    private String motivos;

    @Column(name = "motivo_otro", length = 200)
    private String motivoOtro;

    @Column(name = "impresion_diagnostica", columnDefinition = "TEXT")
    private String impresionDiagnostica;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}