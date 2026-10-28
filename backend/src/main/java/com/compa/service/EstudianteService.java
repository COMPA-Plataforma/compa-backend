package com.compa.service;

import com.compa.dto.request.CreateEstudianteAccountRequest;
import com.compa.dto.request.DeactivateEstudianteRequest;
import com.compa.dto.request.EstudianteListDTO;
import com.compa.dto.request.ReactivateEstudianteRequest;
import com.compa.enums.DocumentType;
import com.compa.enums.EstudianteStatus;
import com.compa.enums.Role;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EstudianteService {
    private final EstudianteRepository estudianteRepository;
    private final ConsentService consentService;
    private final OrientadorRepository orientadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final GuardianService guardianService;

    public EstudianteService(EstudianteRepository estudianteRepository,
            ConsentService consentService,
            OrientadorRepository orientadorRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            GuardianService guardianService) {
        this.estudianteRepository = estudianteRepository;
        this.consentService = consentService;
        this.orientadorRepository = orientadorRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.guardianService = guardianService;
    }

    // CRITERIO: El orientador crea una cuenta de estudiante desde su panel
    // El sistema genera contraseña temporal y envía email de activación
    @Transactional
    public Estudiante createEstudianteAccount(Long orientadorId, CreateEstudianteAccountRequest request) {
        Orientador orientador = orientadorRepository.findById(orientadorId)
                .orElseThrow(() -> new ResourceNotFoundException("Orientador", orientadorId));

        if (estudianteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Ya existe un estudiante con ese email");
        }

        // Validar documento único
        if (estudianteRepository.existsByIdentityDocument(request.getIdentityDocument())) {
            throw new BusinessException("Ese documento ya está registrado");
        }

        // Los estudiantes con tarjeta de identidad son menores de edad: se exige el acudiente
        boolean esMenor = DocumentType.TARJETA_DE_IDENTIDAD.name().equals(request.getDocumentType());
        if (esMenor && request.getAcudiente() == null) {
            throw new BusinessException(
                    "Los estudiantes con tarjeta de identidad son menores de edad: "
                            + "debes registrar los datos del acudiente y la fecha de su autorizacion");
        }

        // Generar contraseña temporal y token de activación
        String tempPassword = generateTempPassword();
        String activationToken = UUID.randomUUID().toString();

        Estudiante estudiante = new Estudiante();
        estudiante.setName(request.getName());
        estudiante.setLastName(request.getLastName());
        estudiante.setEmail(request.getEmail());
        estudiante.setPassword(passwordEncoder.encode(tempPassword));
        estudiante.setTempPassword(tempPassword);
        estudiante.setActivationToken(activationToken);
        estudiante.setActivationTokenExpiresAt(LocalDateTime.now().plusHours(48));
        estudiante.setAccountActivated(false);
        estudiante.setOrientador(orientador);
        estudiante.setIdentityDocument(request.getIdentityDocument());
        estudiante.setDocumentType(DocumentType.valueOf(request.getDocumentType()));
        estudiante.setPhoneNumber(request.getPhoneNumber());
        estudiante.setStatus(EstudianteStatus.ACTIVO);
        estudiante.setRole(Role.ESTUDIANTE);

        estudiante = estudianteRepository.save(estudiante);
        consentService.generateConsentsForEstudiante(estudiante);

        // Guardar el acudiente y la fecha en que autorizo (solo aplica a menores)
        if (esMenor) {
            guardianService.saveGuardian(estudiante.getId(), request.getAcudiente());
        }

        // Enviar email con credenciales al estudiante
        try {
            emailService.sendEstudianteCredentials(
                    estudiante.getEmail(),
                    estudiante.getName(),
                    tempPassword,
                    activationToken);
        } catch (Exception e) {
            log.warn("No se pudo enviar email a {}: {}", estudiante.getEmail(), e.getMessage());
        }

        return estudiante;
    }

    // CRITERIO: El estudiante activa su cuenta desde el link recibido por email
    @Transactional
    public Estudiante activateEstudianteAccount(String token) {
        Estudiante estudiante = estudianteRepository.findByActivationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido o ya usado"));

        if (estudiante.getActivationTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El enlace de activación ha vencido.");
        }

        estudiante.setAccountActivated(true);
        estudiante.setActivationToken(null);
        estudiante.setActivationTokenExpiresAt(null);
        estudiante.setTempPassword(null);

        return estudianteRepository.save(estudiante);
    }

    // CRITERIO: El orientador puede editar datos de contacto (correo, celular)
    @Transactional
    public Estudiante updateContactInfo(Long id, String newEmail, String newPhone) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado con ID: " + id));
        estudiante.setEmail(newEmail);
        estudiante.setPhoneNumber(newPhone);
        return estudianteRepository.save(estudiante);
    }

    // Método adicional para listar activos (útil para el frontend en React)
    public List<Estudiante> getAllEstudiantes() {
        return estudianteRepository.findByStatus(EstudianteStatus.ACTIVO);
    }

    // Listar estudiantes inactivos
    public List<Estudiante> getInactiveEstudiantes() {
        return estudianteRepository.findByStatus(EstudianteStatus.INACTIVO);
    }

    // Listar estudiantes vinculados a un orientador específico
    public List<Estudiante> getEstudiantesByOrientador(Long orientadorId) {
        return estudianteRepository.findByOrientadorId(orientadorId);
    }

    // Obtener estudiante por id
    @Transactional(readOnly = true)
    public Estudiante getEstudianteById(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
    }

    // Desactivar estudiante (baja lógica) - requiere motivo
    @Transactional
    public Estudiante deactivateEstudiante(Long id, DeactivateEstudianteRequest request) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
        if (EstudianteStatus.INACTIVO.equals(estudiante.getStatus())) {
            throw new BusinessException("El estudiante ya se encuentra inactivo.");
        }
        estudiante.setStatus(EstudianteStatus.INACTIVO);
        estudiante.setDeactivationReason(request.getReason());
        estudiante.setDeactivatedAt(LocalDateTime.now());
        return estudianteRepository.save(estudiante);
    }

    // Reactivar estudiante - permite actualizar info básica
    @Transactional
    public Estudiante reactivateEstudiante(Long id, ReactivateEstudianteRequest request) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
        if (EstudianteStatus.ACTIVO.equals(estudiante.getStatus())) {
            throw new BusinessException("El estudiante ya se encuentra activo.");
        }
        estudiante.setStatus(EstudianteStatus.ACTIVO);
        estudiante.setDeactivationReason(null);
        estudiante.setDeactivatedAt(null);
        if (request.getName() != null && !request.getName().isBlank())
            estudiante.setName(request.getName());
        if (request.getLastName() != null && !request.getLastName().isBlank())
            estudiante.setLastName(request.getLastName());
        if (request.getDocumentType() != null)
            estudiante.setDocumentType(request.getDocumentType());
        if (request.getPhoneNumber() != null)
            estudiante.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null)
            estudiante.setEmail(request.getEmail());
        return estudianteRepository.save(estudiante);
    }

    public List<EstudianteListDTO> getAllEstudiantesFiltered(String search, EstudianteStatus status, String condition) {
        List<Estudiante> estudiantes;

        if (search != null && !search.isEmpty()) {
            estudiantes = estudianteRepository.findByNameContainingIgnoreCaseOrIdentityDocumentContaining(search, search);
        } else if (condition != null && !condition.isEmpty()) {
            estudiantes = estudianteRepository.findByPrimaryCondition(condition);
        } else if (status != null) {
            estudiantes = estudianteRepository.findByStatus(status);
        } else {
            estudiantes = estudianteRepository.findAll();
        }

        return estudiantes.stream().map(p -> new EstudianteListDTO(
                p.getName(),
                p.getIdentityDocument(),
                (p.getClinicalInfo() != null) ? p.getClinicalInfo().getMainCondition() : "Sin asignar",
                p.getStatus())).collect(Collectors.toList());
    }

    // Genera una contraseña temporal aleatoria de 10 caracteres
    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}