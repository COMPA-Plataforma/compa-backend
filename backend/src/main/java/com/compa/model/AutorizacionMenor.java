package com.compa.model;
import com.compa.enums.EstadoAutorizacionMenor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
import java.time.LocalDateTime;
 
// Autorización del representante legal de un estudiante menor de edad (tarjeta de identidad).
// Aquí van los datos del trámite; el documento escaneado está en AutorizacionMenorArchivo.
@Entity
@Table(name = "autorizaciones_menor")
@Getter
@Setter
@NoArgsConstructor
public class AutorizacionMenor {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "estudiante_id", nullable = false, unique = true)
    private Estudiante estudiante;
 
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAutorizacionMenor estado = EstadoAutorizacionMenor.PENDIENTE;
 
    // Versión del texto del documento que se descargó/firmó
    @Column(name = "version_documento", length = 10)
    private String versionDocumento;
 
    // Datos del archivo que subió el estudiante (el contenido está en otra tabla)
    @Column(name = "archivo_nombre", length = 160)
    private String archivoNombre;
 
    @Column(name = "archivo_tipo", length = 40)
    private String archivoTipo;
 
    @Column(name = "archivo_tamano")
    private Long archivoTamano;
 
    @Column(name = "subido_en")
    private LocalDateTime subidoEn;
 
    // Revisión del psicoorientador
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_id")
    private Orientador revisadoPor;
 
    @Column(name = "revisado_en")
    private LocalDateTime revisadoEn;
 
    @Column(name = "motivo_rechazo", length = 500)
    private String motivoRechazo;
 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
 
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}