package com.compa.model;
import com.compa.enums.MotivoConsulta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "atenciones")
@Getter
@Setter
@NoArgsConstructor
public class Atencion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String anamnesis;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atencion_motivos", joinColumns = @JoinColumn(name = "atencion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private Set<MotivoConsulta> motivos = new LinkedHashSet<>();

    @Column(name = "motivo_otro", length = 200)
    private String motivoOtro;

    @Column(name = "impresion_diagnostica", length = 300)
    private String impresionDiagnostica;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
}
