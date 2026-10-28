package com.compa.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.compa.dto.response.RiskLevelHistoryResponse;
import com.compa.dto.response.RiskLevelResponse;
import com.compa.service.RiskLevelService;

@RestController
@CrossOrigin(origins = "*")
public class RiskLevelController {
    private final RiskLevelService riskLevelService;

    public RiskLevelController(RiskLevelService riskLevelService) {
        this.riskLevelService = riskLevelService;
    }

    // Obtener nivel de riesgo actual de un estudiante
    @GetMapping("/api/estudiantes/{estudianteId}/risk-level")
    public ResponseEntity<RiskLevelResponse> getCurrentRiskLevel(
            @PathVariable Long estudianteId) {
        return ResponseEntity.ok(riskLevelService.getCurrentRiskLevel(estudianteId));
    }

    // Evaluar y guardar nivel de riesgo de un estudiante
    @PostMapping("/api/estudiantes/{estudianteId}/risk-level/evaluate")
    public ResponseEntity<RiskLevelResponse> evaluateEstudiante(
            @PathVariable Long estudianteId) {
        return ResponseEntity.ok(riskLevelService.evaluateEstudiante(estudianteId));
    }

    // Historial de niveles de riesgo de un estudiante
    @GetMapping("/api/estudiantes/{estudianteId}/risk-level/history")
    public ResponseEntity<List<RiskLevelHistoryResponse>> getRiskLevelHistory(
            @PathVariable Long estudianteId) {
        return ResponseEntity.ok(riskLevelService.getRiskLevelHistory(estudianteId));
    }

    // Evaluar todos los estudiantes activos (disparo manual o scheduler)
    @PostMapping("/api/risk-level/evaluate-all")
    public ResponseEntity<List<RiskLevelResponse>> evaluateAllEstudiantes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(riskLevelService.evaluateAllActiveEstudiantes(userDetails.getUsername()));
    }

    // Ver nivel de riesgo de todos los estudiantes activos
    @GetMapping("/api/risk-level/all")
    public ResponseEntity<List<RiskLevelResponse>> getAllEstudiantesRiskLevel(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(riskLevelService.getAllEstudiantesRiskLevel(userDetails.getUsername()));
    }

}