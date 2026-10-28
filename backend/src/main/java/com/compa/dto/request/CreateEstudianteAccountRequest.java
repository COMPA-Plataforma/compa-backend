package com.compa.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateEstudianteAccountRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El email es obligatorio")
    private String email;

    @NotBlank(message = "El documento es obligatorio")
    private String identityDocument;

    @Pattern(regexp = "CEDULA|TARJETA_DE_IDENTIDAD", message = "Tipo de documento inválido")
    private String documentType = "CEDULA";

    private String phoneNumber;

    // Obligatorio cuando el estudiante tiene tarjeta de identidad (menor de edad)
    @Valid
    private GuardianRequest acudiente;
}