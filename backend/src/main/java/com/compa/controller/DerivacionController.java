package com.compa.controller;

import com.compa.dto.request.DerivacionRequest;
import com.compa.dto.response.DerivacionResponse;
import com.compa.service.DerivacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

// Solo psicoorientadores (ver SecurityConfig). Se deriva siempre desde una alerta.
@RestController
@RequestMapping("/api/alerts/{alertId}/derivacion")
@RequiredArgsConstructor
public class DerivacionController {

    private final DerivacionService derivacionService;

    @PostMapping
    public ResponseEntity<DerivacionResponse> derivar(@PathVariable Long alertId,
            @Valid @RequestBody DerivacionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(derivacionService.derivar(alertId, userDetails.getUsername(), request));
    }
}