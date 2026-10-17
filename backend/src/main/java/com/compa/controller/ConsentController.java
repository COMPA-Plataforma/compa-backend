package com.compa.controller;

import com.compa.model.EstudianteConsent;
import com.compa.service.ConsentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/consents")
@CrossOrigin(origins = "*")
public class ConsentController {

    private final ConsentService consentService;

    public ConsentController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping
    public ResponseEntity<List<EstudianteConsent>> getConsentsByEstudianteId(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(consentService.getConsentsByEstudiante(estudianteId));
    }

    @GetMapping("/pending")
    public ResponseEntity<Boolean> getPendingConsentsByEstudianteId(@PathVariable Long estudianteId) {
        return ResponseEntity.ok(consentService.hasPendingConsents(estudianteId));
    }

    // Aceptar un consentimiento es un acto personal del estudiante:
    // solo se hace desde PATCH /api/estudiantes/me/consents/{consentId}/accept
}