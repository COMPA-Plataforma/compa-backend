package com.compa.model;

import com.compa.enums.MotivoConsulta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Atención (anamnesis) de un estudiante. La anamnesis se compone de varios campos de texto libre. Hay una sola por estudiante y no se
 * elimina; cada cambio queda como una AtencionVersion.
 */
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

    // Entrevista de recepcion. Sigue guardandose en la columna "anamnesis" que ya existia,
    // asi las atenciones registradas antes del cambio conservan su texto sin migrar nada.
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

    // Afiliación a EPS: null = sin dato, true = afiliado, false = no afiliado
    @Column(name = "afiliado_eps")
    private Boolean afiliadoEps;

    // Nombre de la EPS; solo tiene valor cuando afiliadoEps es true
    @Column(name = "eps_nombre", length = 120)
    private String epsNombre;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "atencion_motivos", joinColumns = @JoinColumn(name = "atencion_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "motivo", nullable = false)
    private Set<MotivoConsulta> motivos = new LinkedHashSet<>();

    @Column(name = "motivo_otro", length = 200)
    private String motivoOtro;

    @Column(name = "impresion_diagnostica", columnDefinition = "TEXT")
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