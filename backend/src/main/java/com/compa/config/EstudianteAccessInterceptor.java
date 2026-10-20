package com.compa.config;

import com.compa.exception.ForbiddenException;
import com.compa.model.Orientador;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Regla de la HU "Gestionar usuarios y roles":
 * la información de un estudiante solo la ve el propio estudiante
 * y su psicoorientador asignado.
 */
@Component
@RequiredArgsConstructor
public class EstudianteAccessInterceptor implements HandlerInterceptor {

    private static final String MSG_SIN_ACCESO =
            "No tienes permiso para acceder a la información de este estudiante.";

    // /api/estudiantes/{id}/...  y  /api/notifications/estudiantes/{id}/...
    private static final Pattern RUTA_ESTUDIANTE =
            Pattern.compile("^/api/(?:estudiantes|notifications/estudiantes)/(\\d{1,18})(?:/.*)?$");

    // /api/estudiantes/by-orientador/{orientadorId}
    private static final Pattern RUTA_POR_ORIENTADOR =
            Pattern.compile("^/api/estudiantes/by-orientador/(\\d{1,18})/?$");

    // Listados generales y datos clínicos: solo psicoorientador
    private static final Pattern RUTA_SOLO_ORIENTADOR =
            Pattern.compile("^/api/(?:estudiantes/(?:list|inactive)|risk-level/.*|estudiantes/\\d{1,18}/progress-report(?:/.*)?)/?$");

    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Rutas públicas (sin sesión): no hay nada que verificar aquí
        if (auth == null || !(auth.getPrincipal() instanceof UserDetails user)) {
            return true;
        }

        String path = request.getRequestURI();
        String email = user.getUsername();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("");

        if (RUTA_SOLO_ORIENTADOR.matcher(path).matches()) {
            if (!"ORIENTADOR".equals(role)) {
                throw new ForbiddenException(MSG_SIN_ACCESO);
            }
            return true;
        }

        Matcher porOrientador = RUTA_POR_ORIENTADOR.matcher(path);
        if (porOrientador.matches()) {
            Long orientadorId = Long.valueOf(porOrientador.group(1));
            Long miId = orientadorRepository.findByEmail(email).map(Orientador::getId).orElse(null);
            if (!"ORIENTADOR".equals(role) || miId == null || !miId.equals(orientadorId)) {
                throw new ForbiddenException(MSG_SIN_ACCESO);
            }
            return true;
        }

        Matcher deEstudiante = RUTA_ESTUDIANTE.matcher(path);
        if (deEstudiante.matches()) {
            verificarAccesoEstudiante(Long.valueOf(deEstudiante.group(1)), role, email);
        }

        return true;
    }

    private void verificarAccesoEstudiante(Long estudianteId, String role, String email) {
        switch (role) {
            case "ESTUDIANTE" -> {
                boolean esElMismo = estudianteRepository.findByEmail(email)
                        .map(e -> e.getId().equals(estudianteId))
                        .orElse(false);
                if (!esElMismo) {
                    throw new ForbiddenException(MSG_SIN_ACCESO);
                }
            }
            case "ORIENTADOR" -> {
                // Si el estudiante no existe, deja pasar para que el controlador responda 404
                if (!estudianteRepository.existsById(estudianteId)) {
                    return;
                }
                Long miId = orientadorRepository.findByEmail(email).map(Orientador::getId).orElse(null);
                if (miId == null || !estudianteRepository.existsByIdAndOrientador_Id(estudianteId, miId)) {
                    throw new ForbiddenException(MSG_SIN_ACCESO);
                }
            }
            // Administrador, coordinador u otro rol: no acceden a información de estudiantes
            default -> throw new ForbiddenException(MSG_SIN_ACCESO);
        }
    }
}