package com.compa.controller;

import com.compa.dto.response.DerivacionHistorialResponse;
import com.compa.service.DerivacionConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Consulta del psicoorientador. Cae bajo /api/derivaciones/** (rol ORIENTADOR en SecurityConfig).
@RestController
@RequestMapping("/api/derivaciones")
@RequiredArgsConstructor
public class DerivacionConsultaController {

    private final DerivacionConsultaService consultaService;

    // Vista general: derivaciones de todos mis estudiantes asignados
    @GetMapping("/mias")
    public List<DerivacionHistorialResponse> listarMias(@AuthenticationPrincipal UserDetails userDetails) {
        return consultaService.listarMias(userDetails.getUsername());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public List<DerivacionHistorialResponse> listarPorEstudiante(@PathVariable Long estudianteId,
                                                                 @AuthenticationPrincipal UserDetails userDetails) {
        return consultaService.listarPorEstudiante(estudianteId, userDetails.getUsername());
    }
}