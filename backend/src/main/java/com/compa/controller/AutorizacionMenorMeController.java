package com.compa.controller;

import com.compa.dto.response.AutorizacionMenorResponse;
import com.compa.service.AutorizacionMenorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Lo que ve y hace el estudiante menor de edad con su autorización.
// Es la única parte de la plataforma disponible mientras la autorización no esté aprobada.
@RestController
@RequestMapping("/api/estudiantes/me/autorizacion-menor")
@RequiredArgsConstructor
public class AutorizacionMenorMeController {

    private final AutorizacionMenorService autorizacionService;

    @GetMapping
    public ResponseEntity<AutorizacionMenorResponse> obtener(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autorizacionService.obtenerPropia(userDetails.getUsername()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AutorizacionMenorResponse> subir(@RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(autorizacionService.subir(userDetails.getUsername(), file));
    }
}