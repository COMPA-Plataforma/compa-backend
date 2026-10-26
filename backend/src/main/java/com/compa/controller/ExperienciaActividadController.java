package com.compa.controller;

import com.compa.dto.request.ExperienciaRequest;
import com.compa.dto.response.ExperienciaResponse;
import com.compa.service.ExperienciaActividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
 
// Experiencias del estudiante autenticado sobre las actividades de su plan.
@RestController
@RequestMapping("/api/estudiantes/me/experiencias")
@RequiredArgsConstructor
public class ExperienciaActividadController {
 
    private final ExperienciaActividadService experienciaService;
 
    @PostMapping
    public ResponseEntity<ExperienciaResponse> registrar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExperienciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(experienciaService.registrar(userDetails.getUsername(), request));
    }
 
    // Corregir: solo el mismo dia en que se registro
    @PutMapping("/{experienciaId}")
    public ResponseEntity<ExperienciaResponse> corregir(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long experienciaId,
            @Valid @RequestBody ExperienciaRequest request) {
        return ResponseEntity.ok(experienciaService.corregir(userDetails.getUsername(), experienciaId, request));
    }
 
    // Mis experiencias; ?actividadId= filtra por una actividad
    @GetMapping
    public ResponseEntity<List<ExperienciaResponse>> listar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long actividadId) {
        return ResponseEntity.ok(experienciaService.listar(userDetails.getUsername(), actividadId));
    }
}
