package com.compa.controller;

import com.compa.dto.request.ConclusionRequest;
import com.compa.dto.response.ProgressReportResponse;
import com.compa.model.OrientadorNota;
import com.compa.service.ProgressReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/estudiantes/{estudianteId}/progress-report")
@RequiredArgsConstructor
public class ProgressReportController
{
    private final ProgressReportService progressReportService;

    // Filtro opcional de la adherencia: ?from=yyyy-MM-dd&to=yyyy-MM-dd
    @GetMapping
    public ResponseEntity<ProgressReportResponse> getProgressReport(
            @PathVariable Long estudianteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(progressReportService.getProgressReport(
                estudianteId, userDetails.getUsername(), from, to));
    }

    @PostMapping("/conclusions")
    public ResponseEntity<OrientadorNota> addConclusion(
            @PathVariable Long estudianteId,
            @Valid @RequestBody ConclusionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progressReportService.addConclusion(estudianteId, request));
    }
}