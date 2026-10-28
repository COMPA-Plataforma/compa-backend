package com.compa.service;

import com.compa.dto.request.GuardianRequest;
import com.compa.dto.response.GuardianResponse;
import com.compa.enums.DocumentType;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Guardian;
import com.compa.model.Estudiante;
import com.compa.repository.GuardianRepository;
import com.compa.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuardianService {
    private final GuardianRepository guardianRepository;
    private final EstudianteRepository estudianteRepository;

    public GuardianService(GuardianRepository guardianRepository, EstudianteRepository estudianteRepository) {
        this.guardianRepository = guardianRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional
    public GuardianResponse saveGuardian(Long estudianteId, GuardianRequest request) {
        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        if (estudiante.getDocumentType() != DocumentType.TARJETA_DE_IDENTIDAD) {
            throw new BusinessException(
                    "El acudiente solo aplica para estudiantes con Tarjeta de Identidad. " +
                            "Este estudiante tiene Cédula de Ciudadanía.");
        }

        Guardian guardian = guardianRepository.findByEstudianteId(estudianteId)
                .orElse(new Guardian());

        guardian.setName(request.getName());
        guardian.setLastName(request.getLastName());
        guardian.setDocumentType(request.getDocumentType());
        guardian.setIdentityDocument(request.getIdentityDocument());
        guardian.setRelationship(request.getRelationship());
        guardian.setEmail(request.getEmail());
        guardian.setPhoneNumber(request.getPhoneNumber());
        guardian.setFechaAutorizacion(request.getFechaAutorizacion());
        guardian.setEstudiante(estudiante);

        Guardian saved = guardianRepository.save(guardian);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public GuardianResponse getGuardianByEstudiante(Long estudianteId) {
        estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));

        Guardian guardian = guardianRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new BusinessException(
                        "El estudiante con ID " + estudianteId + " no tiene acudiente registrado."));

        return toResponse(guardian);
    }

    @Transactional
    public void deleteGuardian(Long estudianteId) {
        Guardian guardian = guardianRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new BusinessException(
                        "El estudiante con ID " + estudianteId + " no tiene acudiente registrado."));
        guardianRepository.delete(guardian);
    }

    private GuardianResponse toResponse(Guardian g) {
        return new GuardianResponse(
                g.getId(),
                g.getName(),
                g.getLastName(),
                g.getDocumentType(),
                g.getIdentityDocument(),
                g.getRelationship(),
                g.getEmail(),
                g.getPhoneNumber(),
                g.getEstudiante().getId(),
                g.getFechaAutorizacion()
        );
    }

}
