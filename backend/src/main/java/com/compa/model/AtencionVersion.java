package com.compa.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String anamnesis;

    // Códigos de los motivos separados por coma
    @Column(columnDefinition = "TEXT")
    private String motivos;

    @Column(name = "motivo_otro", length = 200)
    private String motivoOtro;

    @Column(name = "impresion_diagnostica", length = 300)
    private String impresionDiagnostica;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
    
}
