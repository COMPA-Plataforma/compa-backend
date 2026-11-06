package com.compa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Contenido del documento escaneado. Va en una tabla aparte para no traer los bytes
// cada vez que se consulta el estado de la autorización.
@Entity
@Table(name = "autorizaciones_menor_archivos")
@Getter
@Setter
@NoArgsConstructor
public class AutorizacionMenorArchivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autorizacion_id", nullable = false, unique = true)
    private AutorizacionMenor autorizacion;

    // bytea en PostgreSQL
    @JsonIgnore
    @Column(nullable = false)
    private byte[] contenido;
}