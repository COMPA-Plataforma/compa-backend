package com.compa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.compa.enums.EstadoValidacion;
import com.compa.enums.TaskBarrier;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_check_ins")
@Data
@NoArgsConstructor
public class TaskCheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "check_in_id", nullable = false)
    @JsonIgnore
    private DailyCheckIn checkIn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private HabitTask task;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Enumerated(EnumType.STRING)
    @Column(name = "barrier")
    private TaskBarrier barrier;

    // Comentario opcional del estudiante sobre la actividad que informo
    @Column(name = "comment", length = 500)
    private String comment;

    // Validacion del psicoorientador en la sesion. Es null mientras no se ha validado.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_validacion")
    private EstadoValidacion estadoValidacion;

    @Column(name = "validada_en")
    private LocalDateTime validadaEn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "validada_por_id")
    private Orientador validadaPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Si el estudiante cambia lo que informo despues de que el psicoorientador lo valido,
    // la validacion deja de aplicar y hay que validarlo de nuevo.
    public void setCompleted(boolean completed) {
        if (this.id != null && this.completed != completed) {
            limpiarValidacion();
        }
        this.completed = completed;
    }

    public void limpiarValidacion() {
        this.estadoValidacion = null;
        this.validadaEn = null;
        this.validadaPor = null;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}