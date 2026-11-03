package com.compa.controller;

import com.compa.dto.response.DerivacionEstudianteResponse;
import com.compa.service.DerivacionEstudianteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Derivaciones del estudiante autenticado. Solo lectura: no hay POST, PUT ni DELETE.
@RestController
@RequestMapping("/api/estudiantes/me/derivaciones")
@RequiredArgsConstructor
public class EstudianteDerivacionController {

    private final DerivacionEstudianteService derivacionEstudianteService;

    @GetMapping
    public List<DerivacionEstudianteResponse> listar(@AuthenticationPrincipal UserDetails userDetails) {
        return derivacionEstudianteService.listarMias(userDetails.getUsername());
    }
}