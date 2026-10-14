package com.compa.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Sin esto, cualquier error interno (500) se convierte en un 403 vacio y el front cierra sesion
                        .requestMatchers("/error").permitAll()
                        // El estudiante activa su cuenta desde el link del email sin estar autenticado
                        .requestMatchers(HttpMethod.POST, "/api/estudiantes/activate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/notifications/actions/*/complete-task").permitAll()
                        // La atención (anamnesis) es solo para psicoorientadores; el estudiante no accede
                        .requestMatchers("/api/estudiantes/*/atencion/**", "/api/atenciones/**")
                        .hasRole("ORIENTADOR")
                        // Los seguimientos de sesion tambien son solo del psicoorientador
                        .requestMatchers("/api/estudiantes/*/seguimientos/**")
                        .hasRole("ORIENTADOR")
                        // Derivar desde una alerta es solo del psicoorientador
                        .requestMatchers("/api/alerts/*/derivacion/**")
                        .hasRole("ORIENTADOR")
                        // Crear/editar/consultar planes por id de estudiante es del psicoorientador;
                        // el estudiante solo lee su propio plan en /api/estudiantes/me/plan
                        .requestMatchers("/api/estudiantes/*/habit-plans/**")
                        .hasRole("ORIENTADOR")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}