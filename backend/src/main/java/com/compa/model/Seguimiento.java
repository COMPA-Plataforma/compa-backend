package com.compa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Seguimiento: registro fechado de lo trabajado en una sesión con un estudiante.
 * Un estudiante puede tener varios. Nunca se borra: "eliminar" lo marca como anulado
 * y sigue apareciendo en el historial.
 */
@Entity
@Table(name = "seguimientos")
@Getter
@Setter
@NoArgsConstructor
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    // Psicoorientador que lo registró
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orientador_id", nullable = false)
    private Orientador autor;

    // Fecha de la sesión
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    // Marcas de la sesion (asistencia y proceso)
    @Column(name = "falto_cita", nullable = false, columnDefinition = "boolean default false")
    private boolean faltoCita;

    @Column(name = "falto_primera_cita", nullable = false, columnDefinition = "boolean default false")
    private boolean faltoPrimeraCita;

    @Column(name = "cancelada_estudiante", nullable = false, columnDefinition = "boolean default false")
    private boolean canceladaEstudiante;

    @Column(name = "cancelada_departamento", nullable = false, columnDefinition = "boolean default false")
    private boolean canceladaDepartamento;

    @Column(name = "cita_virtual", nullable = false, columnDefinition = "boolean default false")
    private boolean citaVirtual;

    @Column(name = "primera_vez", nullable = false, columnDefinition = "boolean default false")
    private boolean primeraVez;

    @Column(name = "cierre_proceso", nullable = false, columnDefinition = "boolean default false")
    private boolean cierreProceso;

    @Column(name = "remision", nullable = false, columnDefinition = "boolean default false")
    private boolean remision;

    @Column(nullable = false)
    private boolean anulado = false;

    @Column(name = "anulado_at")
    private LocalDateTime anuladoAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anulado_por_id")
    private Orientador anuladoPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}