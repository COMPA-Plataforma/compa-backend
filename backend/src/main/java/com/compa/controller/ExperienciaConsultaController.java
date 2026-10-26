package com.compa.controller;

import com.compa.dto.response.ExperienciaResponse;
import com.compa.service.ExperienciaConsultaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// Para el psicoorientador. El estudiante usa /api/estudiantes/me/experiencias (otro controlador).
@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/experiencias")
@RequiredArgsConstructor
public class ExperienciaConsultaController {

    private final ExperienciaConsultaService experienciaConsultaService;

    // Filtros opcionales: ?desde=yyyy-MM-dd&hasta=yyyy-MM-dd&actividadId=
    @GetMapping
    public ResponseEntity<List<ExperienciaResponse>> listar(@PathVariable Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long actividadId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(experienciaConsultaService.listar(
                estudianteId, userDetails.getUsername(), desde, hasta, actividadId));
    }
}