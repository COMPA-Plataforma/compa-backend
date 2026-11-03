package com.compa.controller;

import com.compa.dto.request.NovedadDerivacionRequest;
import com.compa.dto.response.NovedadDerivacionResponse;
import com.compa.service.DerivacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Registrar novedades es solo del psicoorientador asignado (ver SecurityConfig y DerivacionService).
// No hay PUT ni DELETE: las novedades forman el historial y no se modifican.
@RestController
@RequestMapping("/api/derivaciones/{derivacionId}/novedades")
@RequiredArgsConstructor
public class NovedadDerivacionController {

    private final DerivacionService derivacionService;

    @PostMapping
    public ResponseEntity<NovedadDerivacionResponse> registrar(@PathVariable Long derivacionId,
                                                               @Valid @RequestBody NovedadDerivacionRequest request,
                                                               @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(derivacionService.registrarNovedad(derivacionId, userDetails.getUsername(), request));
    }
}