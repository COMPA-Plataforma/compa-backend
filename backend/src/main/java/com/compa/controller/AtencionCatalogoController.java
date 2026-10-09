package com.compa.controller;

import com.compa.dto.response.AtencionResponse.MotivoItem;
import com.compa.service.AtencionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/atenciones")
@RequiredArgsConstructor
public class AtencionCatalogoController {

    private final AtencionService atencionService;

    @GetMapping("/motivos")
    public ResponseEntity<List<MotivoItem>> motivos() {
        return ResponseEntity.ok(atencionService.catalogoMotivos());
    }
}