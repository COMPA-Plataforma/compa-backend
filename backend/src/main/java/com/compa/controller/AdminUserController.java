package com.compa.controller;

import com.compa.dto.request.CreateAdminUserRequest;
import com.compa.dto.request.UpdateAdminUserRequest;
import com.compa.dto.response.AdminUserResponse;
import com.compa.enums.Role;
import com.compa.enums.UserType;
import com.compa.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Solo accesible para ADMINISTRADOR (regla en SecurityConfig)
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<AdminUserResponse>> list(
            @RequestParam(required = false) UserType type,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminUserService.list(type, role, active, search));
    }

    @GetMapping("/{type}/{id}")
    public ResponseEntity<AdminUserResponse> get(
            @PathVariable UserType type,
            @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.get(type, id));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> create(
            @Valid @RequestBody CreateAdminUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminUserService.create(request));
    }

    @PutMapping("/{type}/{id}")
    public ResponseEntity<AdminUserResponse> update(
            @PathVariable UserType type,
            @PathVariable Long id,
            @Valid @RequestBody UpdateAdminUserRequest request,
            @AuthenticationPrincipal UserDetails actor) {
        return ResponseEntity.ok(adminUserService.update(type, id, request, actor.getUsername()));
    }

    @PatchMapping("/{type}/{id}/deactivate")
    public ResponseEntity<AdminUserResponse> deactivate(
            @PathVariable UserType type,
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails actor) {
        return ResponseEntity.ok(adminUserService.deactivate(type, id, actor.getUsername()));
    }

    @PatchMapping("/{type}/{id}/reactivate")
    public ResponseEntity<AdminUserResponse> reactivate(
            @PathVariable UserType type,
            @PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.reactivate(type, id));
    }
}