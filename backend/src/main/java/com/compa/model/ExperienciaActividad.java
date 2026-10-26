package com.compa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Experiencia que el estudiante cuenta sobre una actividad de su plan:
 * como le fue, que aprendio o que dificultades tuvo. Una actividad puede tener varias.
 */
@Entity
@Table(name = "experiencias_actividad")
@Getter
@Setter
@NoArgsConstructor
public class ExperienciaActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    // Actividad del plan a la que corresponde
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habit_task_id", nullable = false)
    private HabitTask actividad;

    @Column(nullable = false, length = 1000)
    private String texto;

    // Dia en que se registro la experiencia (hora de Colombia)
    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Solo se llena si el estudiante corrige la experiencia
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}