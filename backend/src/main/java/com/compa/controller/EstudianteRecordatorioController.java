package com.compa.controller;

import com.compa.dto.request.ConfiguracionRecordatorioRequest;
import com.compa.dto.response.ConfiguracionRecordatorioResponse;
import com.compa.service.ConfiguracionRecordatorioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Configuración de recordatorios del estudiante autenticado (no recibe id: solo la propia).
@RestController
@RequestMapping("/api/estudiantes/me/recordatorios")
@RequiredArgsConstructor
public class EstudianteRecordatorioController {

    private final ConfiguracionRecordatorioService configuracionService;

    @GetMapping
    public ResponseEntity<ConfiguracionRecordatorioResponse> obtener(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(configuracionService.obtener(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<ConfiguracionRecordatorioResponse> guardar(
            @Valid @RequestBody ConfiguracionRecordatorioRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(configuracionService.guardar(userDetails.getUsername(), request));
    }

    // Vuelve a la hora predeterminada del sistema
    @DeleteMapping
    public ResponseEntity<ConfiguracionRecordatorioResponse> restablecer(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(configuracionService.restablecer(userDetails.getUsername()));
    }
}