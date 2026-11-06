package com.compa.controller;

import com.compa.dto.response.EstrategiaResponse;
import com.compa.service.EstrategiaPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Lo que ve el estudiante dentro de su plan: solo las estrategias vigentes del plan activo.
// La ruta cuelga de /plan para que no choque con la regla del orientador /api/estudiantes/*/estrategias.
@RestController
@RequestMapping("/api/estudiantes/me/plan/estrategias")
@RequiredArgsConstructor
public class EstrategiaPlanMeController {

    private final EstrategiaPlanService estrategiaService;

    @GetMapping
    public ResponseEntity<List<EstrategiaResponse>> vigentes(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(estrategiaService.vigentesDelPlanActivo(userDetails.getUsername()));
    }
}