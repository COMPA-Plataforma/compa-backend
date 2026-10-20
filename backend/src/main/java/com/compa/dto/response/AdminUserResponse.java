package com.compa.dto.response;

import com.compa.enums.Role;
import com.compa.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponse
{
    private Long id;
    private UserType type;
    private String name;
    private String lastName;
    private String email;
    private Role role;
    private String status;     // ACTIVE / PENDING / INACTIVE (personal) o ACTIVO / INACTIVO (estudiante)
    private boolean active;    // true si puede iniciar sesión (no está desactivado)
    private LocalDateTime createdAt;
}