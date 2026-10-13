package com.compa.controller;

import com.compa.dto.request.SeguimientoRequest;
import com.compa.dto.response.SeguimientoResponse;
import com.compa.service.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Solo psicoorientadores (ver SecurityConfig). El estudiante no tiene ningún endpoint de seguimientos.
@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/seguimientos")
@RequiredArgsConstructor
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    @PostMapping
    public ResponseEntity<SeguimientoResponse> registrar(@PathVariable Long estudianteId,
            @Valid @RequestBody SeguimientoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(seguimientoService.registrar(estudianteId, userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<List<SeguimientoResponse>> historial(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(seguimientoService.historial(estudianteId, userDetails.getUsername()));
    }

    // "Eliminar" anula el seguimiento y lo conserva en el historial
    @DeleteMapping("/{seguimientoId}")
    public ResponseEntity<SeguimientoResponse> anular(@PathVariable Long estudianteId,
            @PathVariable Long seguimientoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(seguimientoService.anular(estudianteId, seguimientoId, userDetails.getUsername()));
    }
}