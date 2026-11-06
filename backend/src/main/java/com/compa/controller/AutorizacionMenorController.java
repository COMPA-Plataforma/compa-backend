package com.compa.controller;

import com.compa.dto.request.RevisionAutorizacionRequest;
import com.compa.dto.response.AutorizacionMenorResponse;
import com.compa.service.AutorizacionMenorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Revisión del documento por el psicoorientador asignado (ver SecurityConfig: solo rol ORIENTADOR).
@RestController
@RequiredArgsConstructor
public class AutorizacionMenorController {

    private final AutorizacionMenorService autorizacionService;

    @GetMapping("/api/estudiantes/{estudianteId}/autorizacion-menor")
    public ResponseEntity<AutorizacionMenorResponse> obtener(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autorizacionService.obtenerDeEstudiante(estudianteId, userDetails.getUsername()));
    }

    // El documento escaneado, para verlo en pantalla
    @GetMapping("/api/estudiantes/{estudianteId}/autorizacion-menor/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long estudianteId,
            @AuthenticationPrincipal UserDetails userDetails) {
        AutorizacionMenorService.Archivo a = autorizacionService.descargar(estudianteId, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.tipo()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(a.nombre()).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(a.contenido());
    }

    @PutMapping("/api/estudiantes/{estudianteId}/autorizacion-menor/revision")
    public ResponseEntity<AutorizacionMenorResponse> revisar(@PathVariable Long estudianteId,
            @Valid @RequestBody RevisionAutorizacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autorizacionService.revisar(estudianteId, userDetails.getUsername(), request));
    }

    // Documentos que esperan revisión, de los estudiantes del psicoorientador
    @GetMapping("/api/autorizaciones-menor/pendientes")
    public ResponseEntity<List<AutorizacionMenorResponse>> pendientes(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autorizacionService.pendientesDeRevision(userDetails.getUsername()));
    }
}