package com.compa.model;

import com.compa.enums.EstadoEstrategia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

// Estrategia que el psicoorientador trabajó con el estudiante dentro de su plan de acompañamiento
@Entity
@Table(name = "estrategias_plan")
@Getter
@Setter
@NoArgsConstructor
public class EstrategiaPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    // Plan activo en el momento en que se registró la estrategia
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private HabitPlan plan;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    // Fecha de la sesión en que se trabajó
    @Column(name = "fecha_sesion", nullable = false)
    private LocalDate fechaSesion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEstrategia estado = EstadoEstrategia.VIGENTE;

    @Column(name = "creada_en", nullable = false, updatable = false)
    private LocalDateTime creadaEn;

    // Fecha del último cambio (solo si se actualizó)
    @Column(name = "actualizada_en")
    private LocalDateTime actualizadaEn;

    @Column(name = "anulada_en")
    private LocalDateTime anuladaEn;

    @PrePersist
    protected void onCreate() {
        this.creadaEn = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoEstrategia.VIGENTE;
        }
    }
}