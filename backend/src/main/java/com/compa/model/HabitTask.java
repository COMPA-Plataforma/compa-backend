package com.compa.model;

import com.compa.enums.TaskPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "habit_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_plan_id", nullable = false)
    private HabitPlan habitPlan;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIA;

    @Column(nullable = false)
    private boolean mandatory = false;

    @Column(name = "weekly_goal")
    private Integer weeklyGoal;

    @ElementCollection
    @CollectionTable(
            name = "habit_task_specific_days",
            joinColumns = @JoinColumn(name = "habit_task_id")
    )
    @Column(name = "specific_days")
    private List<String> specificDays = new ArrayList<>();

        // Fecha límite de la actividad (opcional). Se usa en lugar de una frecuencia.
    @Column(name = "due_date")
    private LocalDate dueDate;

    // Fecha de la sesión en la que se acordó la actividad. Es null en actividades
    // antiguas; en ese caso se usa la fecha de creación.
    @Column(name = "agreed_date")
    private LocalDate agreedDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}
