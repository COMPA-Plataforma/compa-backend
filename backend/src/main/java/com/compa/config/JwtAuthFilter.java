package com.compa.config;

import com.compa.enums.EstudianteStatus;
import com.compa.enums.OrientadorStatus;
import com.compa.enums.Role;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import com.compa.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final OrientadorRepository orientadorRepository;
    private final EstudianteRepository estudianteRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);

        if (!jwtService.isTokenValid(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email = jwtService.extractEmail(jwt);
        final String tokenRole = jwtService.extractRole(jwt);

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (Role.ESTUDIANTE.name().equals(tokenRole)) {
                // Estudiante: debe existir y no estar desactivado
                estudianteRepository.findByEmail(email)
                        .filter(e -> e.getStatus() != EstudianteStatus.INACTIVO)
                        .ifPresent(e -> setAuthentication(request, email, Role.ESTUDIANTE.name()));
            } else {
                // Personal (psicoorientador, coordinador, administrador): debe existir y estar ACTIVO.
                // El rol se toma de la base de datos, así un cambio de rol aplica de inmediato.
                orientadorRepository.findByEmail(email)
                        .filter(o -> o.getStatus() == OrientadorStatus.ACTIVE)
                        .ifPresent(o -> setAuthentication(request, email, o.getRole().name()));
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, String email, String role) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                new User(email, "", List.of(new SimpleGrantedAuthority("ROLE_" + role))),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
}