package com.compa.controller;

import com.compa.dto.request.AtencionRequest;
import com.compa.dto.response.AtencionResponse;
import com.compa.dto.response.AtencionVersionResponse;
import com.compa.service.AtencionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/atencion")
@RequiredArgsConstructor
public class AtencionController {

    private final AtencionService atencionService;

    @PostMapping
    public ResponseEntity<AtencionResponse> registrar(@PathVariable Long estudianteId,
            @Valid @RequestBody AtencionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(atencionService.registrar(estudianteId, userDetails.getUsername(), request));
    }

    @GetMapping
    public ResponseEntity<AtencionResponse> consultar(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(atencionService.consultar(estudianteId, userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<AtencionResponse> actualizar(@PathVariable Long estudianteId,
            @Valid @RequestBody AtencionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(atencionService.actualizar(estudianteId, userDetails.getUsername(), request));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<AtencionVersionResponse>> historial(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(atencionService.historial(estudianteId, userDetails.getUsername()));
    }
}