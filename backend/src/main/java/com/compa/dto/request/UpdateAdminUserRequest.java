package com.compa.dto.request;

import com.compa.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAdminUserRequest
{
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El correo institucional es obligatorio")
    private String email;

    // Solo aplica al personal. Para estudiantes se ignora (siempre son ESTUDIANTE).
    private Role role;
}