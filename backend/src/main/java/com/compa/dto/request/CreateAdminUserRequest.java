package com.compa.dto.request;

import com.compa.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAdminUserRequest
{
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El apellido es obligatorio")
    private String lastName;

    @Email(message = "Debe ser un correo válido")
    @NotBlank(message = "El correo institucional es obligatorio")
    private String email;

    @NotNull(message = "El rol es obligatorio")
    private Role role;

    @NotBlank(message = "La contraseña inicial es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
    private String password;
}