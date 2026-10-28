package com.compa.controller;

import com.compa.dto.request.CreateEstudianteAccountRequest;
import com.compa.dto.request.DeactivateEstudianteRequest;
import com.compa.dto.request.EstudianteListDTO;
import com.compa.dto.request.ReactivateEstudianteRequest;
import com.compa.enums.EstudianteStatus;
import com.compa.model.Estudiante;
import com.compa.service.EstudianteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/estudiantes")
@CrossOrigin(origins = "*") // Permite que React (en otro puerto) se conecte sin bloqueos de CORS
public class EstudianteController {
    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // POST: El orientador crea una cuenta de estudiante desde su panel
    // Genera contraseña temporal y envía email de activación al estudiante
    @PostMapping("/by-orientador/{orientadorId}")
    public ResponseEntity<Estudiante> createEstudianteAccount(
            @PathVariable Long orientadorId,
            @Valid @RequestBody CreateEstudianteAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(estudianteService.createEstudianteAccount(orientadorId, request));
    }

    // POST: El estudiante activa su cuenta desde el link recibido por email
    @PostMapping("/activate")
    public ResponseEntity<Map<String, String>> activateAccount(@RequestParam String token) {
        estudianteService.activateEstudianteAccount(token);
        return ResponseEntity.ok(Map.of("message", "Cuenta activada correctamente"));
    }

    // PATCH: Editar solo datos de contacto
    @PatchMapping("/{id}/contact")
    public ResponseEntity<Estudiante> updateContact(@PathVariable Long id,
            @RequestParam String email,
            @RequestParam String phoneNumber) {
        return ResponseEntity.ok(estudianteService.updateContactInfo(id, email, phoneNumber));
    }

    // GET: Listar todos los estudiantes activos (por defecto)
    @GetMapping
    public ResponseEntity<List<Estudiante>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(estudianteService.getAllEstudiantes(userDetails.getUsername()));
    }

    // GET: Listar estudiantes inactivos
    @GetMapping("/inactive")
    public ResponseEntity<List<Estudiante>> getInactive(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(estudianteService.getInactiveEstudiantes(userDetails.getUsername()));
    }

    // GET: Listar estudiantes vinculados a un orientador específico
    @GetMapping("/by-orientador/{orientadorId}")
    public ResponseEntity<List<Estudiante>> getByOrientador(@PathVariable Long orientadorId) {
        return ResponseEntity.ok(estudianteService.getEstudiantesByOrientador(orientadorId));
    }

    // GET: Obtener estudiante por id
    @GetMapping("/{id}")
    public ResponseEntity<Estudiante> getById(@PathVariable Long id) {
        return ResponseEntity.ok(estudianteService.getEstudianteById(id));
    }

    // PATCH: Desactivar estudiante - requiere motivo
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Estudiante> deactivate(@PathVariable Long id,
            @Valid @RequestBody DeactivateEstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.deactivateEstudiante(id, request));
    }

    // PATCH: Reactivar estudiante - permite actualizar info básica
    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<Estudiante> reactivate(@PathVariable Long id,
            @RequestBody ReactivateEstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.reactivateEstudiante(id, request));
    }

    // GET: Buscar y filtrar estudiantes
    @GetMapping("/list")
    public ResponseEntity<List<EstudianteListDTO>> listEstudiantes(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) EstudianteStatus status,
            @RequestParam(required = false) String condition) {
        return ResponseEntity.ok(estudianteService.getAllEstudiantesFiltered(search, status, condition));
    }

}