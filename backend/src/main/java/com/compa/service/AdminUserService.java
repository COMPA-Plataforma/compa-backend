package com.compa.service;

import com.compa.dto.request.CreateAdminUserRequest;
import com.compa.dto.request.UpdateAdminUserRequest;
import com.compa.dto.response.AdminUserResponse;
import com.compa.enums.EstudianteStatus;
import com.compa.enums.OrientadorStatus;
import com.compa.enums.Role;
import com.compa.enums.UserType;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final OrientadorRepository orientadorRepository;
    private final EstudianteRepository estudianteRepository;
    private final PasswordEncoder passwordEncoder;

    // ---------------------------------------------------------------
    // Consultar
    // ---------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AdminUserResponse> list(UserType type, Role role, Boolean active, String search) {
        List<AdminUserResponse> all = new ArrayList<>();

        if (type == null || type == UserType.PERSONAL) {
            orientadorRepository.findAll().forEach(o -> all.add(toResponse(o)));
        }
        if (type == null || type == UserType.ESTUDIANTE) {
            estudianteRepository.findAll().forEach(e -> all.add(toResponse(e)));
        }

        String term = search == null ? "" : search.trim().toLowerCase();

        return all.stream()
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> active == null || u.isActive() == active)
                .filter(u -> term.isEmpty()
                        || contains(u.getName(), term)
                        || contains(u.getLastName(), term)
                        || contains(u.getEmail(), term))
                .sorted(Comparator.comparing(AdminUserResponse::getLastName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(AdminUserResponse::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserResponse get(UserType type, Long id) {
        return type == UserType.PERSONAL
                ? toResponse(findOrientador(id))
                : toResponse(findEstudiante(id));
    }

    // ---------------------------------------------------------------
    // Crear (solo personal; los estudiantes los crea el psicoorientador)
    // ---------------------------------------------------------------

    @Transactional
    public AdminUserResponse create(CreateAdminUserRequest request) {
        requireStaffRole(request.getRole());

        String email = request.getEmail().trim();
        assertEmailAvailable(email);

        Orientador nuevo = Orientador.builder()
                .name(request.getName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(OrientadorStatus.ACTIVE)
                .build();

        orientadorRepository.save(nuevo);
        log.info("Usuario creado por el administrador: {} ({})", nuevo.getEmail(), nuevo.getRole());
        return toResponse(nuevo);
    }

    // ---------------------------------------------------------------
    // Actualizar
    // ---------------------------------------------------------------

    @Transactional
    public AdminUserResponse update(UserType type, Long id, UpdateAdminUserRequest request, String actorEmail) {
        String email = request.getEmail().trim();

        if (type == UserType.PERSONAL) {
            Orientador o = findOrientador(id);
            boolean esElMismo = o.getEmail().equalsIgnoreCase(actorEmail);

            if (!email.equalsIgnoreCase(o.getEmail())) {
                if (esElMismo) {
                    throw new BusinessException(
                            "No puedes cambiar tu propio correo. Pídele a otro administrador que lo haga.");
                }
                assertEmailAvailable(email);
            }

            if (request.getRole() != null) {
                requireStaffRole(request.getRole());
                if (esElMismo && request.getRole() != Role.ADMINISTRADOR) {
                    throw new BusinessException("No puedes quitarte a ti mismo el rol de administrador.");
                }
                o.setRole(request.getRole());
            }

            o.setName(request.getName().trim());
            o.setLastName(request.getLastName().trim());
            o.setEmail(email);
            return toResponse(orientadorRepository.save(o));
        }

        Estudiante e = findEstudiante(id);
        if (e.getEmail() == null || !email.equalsIgnoreCase(e.getEmail())) {
            assertEmailAvailable(email);
        }
        e.setName(request.getName().trim());
        e.setLastName(request.getLastName().trim());
        e.setEmail(email);
        return toResponse(estudianteRepository.save(e));
    }

    // ---------------------------------------------------------------
    // Desactivar / reactivar (nunca se elimina)
    // ---------------------------------------------------------------

    @Transactional
    public AdminUserResponse deactivate(UserType type, Long id, String actorEmail) {
        if (type == UserType.PERSONAL) {
            Orientador o = findOrientador(id);
            if (o.getEmail().equalsIgnoreCase(actorEmail)) {
                throw new BusinessException("No puedes desactivar tu propia cuenta.");
            }
            if (o.getStatus() == OrientadorStatus.INACTIVE) {
                throw new BusinessException("El usuario ya se encuentra desactivado.");
            }
            o.setStatus(OrientadorStatus.INACTIVE);
            return toResponse(orientadorRepository.save(o));
        }

        Estudiante e = findEstudiante(id);
        if (e.getStatus() == EstudianteStatus.INACTIVO) {
            throw new BusinessException("El usuario ya se encuentra desactivado.");
        }
        e.setStatus(EstudianteStatus.INACTIVO);
        e.setDeactivatedAt(LocalDateTime.now());
        return toResponse(estudianteRepository.save(e));
    }

    @Transactional
    public AdminUserResponse reactivate(UserType type, Long id) {
        if (type == UserType.PERSONAL) {
            Orientador o = findOrientador(id);
            if (o.getStatus() != OrientadorStatus.INACTIVE) {
                throw new BusinessException("El usuario ya se encuentra activo.");
            }
            o.setStatus(OrientadorStatus.ACTIVE);
            return toResponse(orientadorRepository.save(o));
        }

        Estudiante e = findEstudiante(id);
        if (e.getStatus() != EstudianteStatus.INACTIVO) {
            throw new BusinessException("El usuario ya se encuentra activo.");
        }
        e.setStatus(EstudianteStatus.ACTIVO);
        e.setDeactivationReason(null);
        e.setDeactivatedAt(null);
        return toResponse(estudianteRepository.save(e));
    }

    // ---------------------------------------------------------------
    // Auxiliares
    // ---------------------------------------------------------------

    private Orientador findOrientador(Long id) {
        return orientadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private Estudiante findEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
    }

    private void requireStaffRole(Role role) {
        if (role == Role.ESTUDIANTE) {
            throw new BusinessException(
                    "Los estudiantes se crean desde el panel del psicoorientador. Elige un rol del personal.");
        }
    }

    // El correo debe ser único entre el personal y los estudiantes
    private void assertEmailAvailable(String email) {
        if (orientadorRepository.existsByEmailIgnoreCase(email)
                || estudianteRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Ya existe un usuario con ese correo");
        }
    }

    private boolean contains(String value, String term) {
        return value != null && value.toLowerCase().contains(term);
    }

    private AdminUserResponse toResponse(Orientador o) {
        return AdminUserResponse.builder()
                .id(o.getId())
                .type(UserType.PERSONAL)
                .name(o.getName())
                .lastName(o.getLastName())
                .email(o.getEmail())
                .role(o.getRole())
                .status(o.getStatus().name())
                .active(o.getStatus() != OrientadorStatus.INACTIVE)
                .createdAt(o.getCreatedAt())
                .build();
    }

    private AdminUserResponse toResponse(Estudiante e) {
        return AdminUserResponse.builder()
                .id(e.getId())
                .type(UserType.ESTUDIANTE)
                .name(e.getName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .role(Role.ESTUDIANTE)
                .status(e.getStatus().name())
                .active(e.getStatus() != EstudianteStatus.INACTIVO)
                .createdAt(e.getCreatedAt())
                .build();
    }
}