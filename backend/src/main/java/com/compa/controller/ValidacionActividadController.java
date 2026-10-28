package com.compa.controller;

import com.compa.dto.request.ValidacionActividadRequest;
import com.compa.dto.response.ActividadInformadaResponse;
import com.compa.dto.response.ValidacionesResponse;
import com.compa.service.ValidacionActividadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

// Solo psicoorientadores (ver SecurityConfig). El estudiante ve el resultado en /api/estudiantes/me/plan/progreso.
@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/validaciones")
@RequiredArgsConstructor
public class ValidacionActividadController {

    private final ValidacionActividadService validacionService;

    // Por defecto lista desde la ultima sesion. Opcional: ?desde=yyyy-MM-dd
    @GetMapping
    public ResponseEntity<ValidacionesResponse> listar(@PathVariable Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(validacionService.listar(estudianteId, userDetails.getUsername(), desde));
    }

    @PutMapping("/{registroId}")
    public ResponseEntity<ActividadInformadaResponse> validar(@PathVariable Long estudianteId,
            @PathVariable Long registroId,
            @Valid @RequestBody ValidacionActividadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(validacionService.validar(estudianteId, registroId, userDetails.getUsername(), request));
    }
}