package com.compa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(name = "comment_text", length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}