package com.compa.controller;

import com.compa.dto.response.InformeMotivosResponse;
import com.compa.enums.MotivoConsulta;
import com.compa.service.InformeMotivosService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

// Solo el coordinador de Bienestar (regla en SecurityConfig). Informe agregado y anonimizado.
@RestController
@RequestMapping("/api/coordinador/informes")
@RequiredArgsConstructor
public class InformeMotivosController {

    private final InformeMotivosService informeMotivosService;

    // Ejemplo: GET /api/coordinador/informes/motivos?desde=2026-09-01&hasta=2026-11-10&motivo=BIENESTAR_EMOCIONAL
    @GetMapping("/motivos")
    public InformeMotivosResponse motivos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) MotivoConsulta motivo) {
        return informeMotivosService.generar(desde, hasta, motivo);
    }
}