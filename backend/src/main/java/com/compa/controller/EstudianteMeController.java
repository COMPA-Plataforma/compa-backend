package com.compa.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.compa.dto.request.DailyCheckInRequest;
import com.compa.dto.response.CheckInDetailResponse;
import com.compa.dto.response.CheckInSummaryResponse;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.DailyCheckIn;
import com.compa.dto.response.PlanEstudianteResponse;
import com.compa.model.Estudiante;
import com.compa.model.EstudianteConsent;
import com.compa.repository.EstudianteRepository;
import com.compa.service.ConsentService;
import com.compa.service.DailyCheckInService;
import com.compa.service.HabitPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes/me")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor

public class EstudianteMeController {
    private final EstudianteRepository estudianteRepository;
    private final DailyCheckInService checkInService;
    private final HabitPlanService habitPlanService;
    private final ConsentService consentService;

    // Obtener datos propios del estudiante autenticado
    @GetMapping
    public ResponseEntity<Estudiante> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(getEstudianteFromToken(userDetails));
    }

    // Check-in de hoy
    @GetMapping("/check-in/today")
    public ResponseEntity<DailyCheckIn> getTodayCheckIn(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(checkInService.getTodayCheckIn(estudiante.getId()));
    }

    // Crear check-in
    @PostMapping("/check-in")
    public ResponseEntity<DailyCheckIn> createCheckIn(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DailyCheckInRequest request) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(checkInService.createCheckIn(estudiante.getId(), request));
    }

    // Editar check-in
    @PutMapping("/check-in")
    public ResponseEntity<DailyCheckIn> updateCheckIn(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody DailyCheckInRequest request) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(checkInService.updateCheckIn(estudiante.getId(), request));
    }

    // Historial últimos 30 días
    @GetMapping("/check-in/last-30-days")
    public ResponseEntity<List<CheckInSummaryResponse>> getLast30Days(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(checkInService.getLast30Days(estudiante.getId()));
    }

    // Detalle de un check-in
    @GetMapping("/check-in/{checkInId}/detail")
    public ResponseEntity<CheckInDetailResponse> getCheckInDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long checkInId) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(checkInService.getCheckInDetail(estudiante.getId(), checkInId));
    }

    // Mensaje de cierre y racha
    @GetMapping("/check-in/closing")
    public ResponseEntity<Map<String, Object>> getClosing(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        int streak = checkInService.getCurrentStreak(estudiante.getId());
        String message = checkInService.getClosingMessage(estudiante.getId());
        return ResponseEntity.ok(Map.of("streak", streak, "message", message));
    }

    // Tareas del día
    @GetMapping("/check-in/today-tasks")
    public ResponseEntity<?> getTodayTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(checkInService.getTodayTasks(estudiante.getId()));
    }

    // Plan activo
    @GetMapping("/plan")
    public ResponseEntity<PlanEstudianteResponse> getActivePlan(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(habitPlanService.getPlanActivoParaEstudiante(estudiante.getId()));
    }

    // Consentimientos
    @GetMapping("/consents")
    public ResponseEntity<List<EstudianteConsent>> getConsents(
            @AuthenticationPrincipal UserDetails userDetails) {
        Estudiante estudiante = getEstudianteFromToken(userDetails);
        return ResponseEntity.ok(consentService.getConsentsByEstudiante(estudiante.getId()));
    }

    // Aceptar consentimiento
    @PatchMapping("/consents/{consentId}/accept")
    public ResponseEntity<EstudianteConsent> acceptConsent(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long consentId) {
        getEstudianteFromToken(userDetails); // verifica que es el estudiante correcto
        return ResponseEntity.ok(consentService.acceptConsent(consentId));
    }

    // Helper — extrae el estudiante del token
    private Estudiante getEstudianteFromToken(UserDetails userDetails) {
        String email = userDetails.getUsername();
        return estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
    }

}
