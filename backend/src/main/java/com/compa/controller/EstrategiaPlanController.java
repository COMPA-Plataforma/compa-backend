package com.compa.controller;

import com.compa.dto.request.EstrategiaRequest;
import com.compa.dto.response.EstrategiaResponse;
import com.compa.service.EstrategiaPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Estrategias de un estudiante: solo el psicoorientador (ver SecurityConfig).
@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/estrategias")
@RequiredArgsConstructor
public class EstrategiaPlanController {

    private final EstrategiaPlanService estrategiaService;

    @PostMapping
    public ResponseEntity<EstrategiaResponse> crear(@PathVariable Long estudianteId,
            @Valid @RequestBody EstrategiaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(estrategiaService.crear(estudianteId, request));
    }

    // Historial en orden cronológico (incluye anuladas)
    @GetMapping
    public ResponseEntity<List<EstrategiaResponse>> historial(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(estrategiaService.historial(estudianteId));
    }

    @PutMapping("/{estrategiaId}")
    public ResponseEntity<EstrategiaResponse> actualizar(@PathVariable Long estudianteId,
            @PathVariable Long estrategiaId, @Valid @RequestBody EstrategiaRequest request) {
        return ResponseEntity.ok(estrategiaService.actualizar(estudianteId, estrategiaId, request));
    }

    // Eliminar = anular: se conserva en el historial
    @DeleteMapping("/{estrategiaId}")
    public ResponseEntity<EstrategiaResponse> anular(@PathVariable Long estudianteId,
            @PathVariable Long estrategiaId) {
        return ResponseEntity.ok(estrategiaService.anular(estudianteId, estrategiaId));
    }
}