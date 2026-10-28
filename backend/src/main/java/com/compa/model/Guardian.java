package com.compa.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.compa.enums.DocumentType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "guardians")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Guardian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del acudiente es obligatorio")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Los apellidos del acudiente son obligatorios")
    @Column(nullable = false)
    private String lastName;

    @NotNull(message = "El tipo de documento del acudiente es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;

    @NotBlank(message = "El documento del acudiente es obligatorio")
    @Column(name = "identity_document", nullable = false)
    private String identityDocument;

    @NotBlank(message = "El parentesco es obligatorio")
    @Column(nullable = false)
    private String relationship;

    @Email(message = "Debe ser un correo válido")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    // Fecha en que el representante legal autorizó el acompañamiento (la registra el psicoorientador).
    // Es null en los acudientes creados antes de este campo.
    @Column(name = "fecha_autorizacion")
    private LocalDate fechaAutorizacion;

    @ManyToOne
    @JsonBackReference
    private Estudiante estudiante;
}