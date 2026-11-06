package com.compa.config;

import com.compa.service.AutorizacionMenorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

// Bloquea a los estudiantes menores de edad cuya autorización del representante legal no está aprobada.
// Solo pueden: iniciar/cerrar sesión, ver su perfil y usar la pantalla de la autorización.
@Component
@RequiredArgsConstructor
public class MenorAutorizacionInterceptor implements HandlerInterceptor {

    public static final String CODIGO = "AUTORIZACION_MENOR_PENDIENTE";
    private static final String MENSAJE =
            "Para usar COMPA falta la autorización de tu representante legal. Súbela y espera la aprobación de tu psicoorientador.";

    private final AutorizacionMenorService autorizacionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || !path.startsWith("/api/") || permitido(request, path)) {
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esEstudiante = auth != null && auth.isAuthenticated() && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ESTUDIANTE".equals(a.getAuthority()));
        if (!esEstudiante || autorizacionService.accesoPermitido(auth.getName())) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"status\":403,\"code\":\"" + CODIGO + "\",\"error\":\"" + MENSAJE + "\"}");
        return false;
    }

    private boolean permitido(HttpServletRequest request, String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/estudiantes/me/autorizacion-menor")
                || (HttpMethod.GET.matches(request.getMethod()) && path.equals("/api/estudiantes/me"));
    }
}