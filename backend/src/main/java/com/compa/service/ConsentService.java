package com.compa.service;

import com.compa.exception.ResourceNotFoundException;
import com.compa.model.ConsentTemplate;
import com.compa.model.Estudiante;
import com.compa.model.EstudianteConsent;
import com.compa.repository.ConsentTemplateRepository;
import com.compa.repository.EstudianteConsentRepository;
import com.compa.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConsentService {
    private final EstudianteConsentRepository estudianteConsentRepository;
    private final ConsentTemplateRepository consentTemplateRepository;
    private final EstudianteRepository estudianteRepository;

    public ConsentService(EstudianteConsentRepository estudianteConsentRepository,
                          ConsentTemplateRepository consentTemplateRepository,
                          EstudianteRepository estudianteRepository) {
        this.estudianteConsentRepository = estudianteConsentRepository;
        this.consentTemplateRepository = consentTemplateRepository;
        this.estudianteRepository = estudianteRepository;
    }

    // Se llama al registrar un estudiante: le crea sus consentimientos pendientes
    @Transactional
    public void generateConsentsForEstudiante(Estudiante estudiante) {
        syncConsents(estudiante);
    }

    // Deja al día los consentimientos del estudiante:
    // 1) crea los que le falten (plantillas nuevas o estudiantes antiguos)
    // 2) marca como pendientes los aceptados con una versión distinta a la vigente
    @Transactional
    public void syncConsents(Estudiante estudiante) {
        List<ConsentTemplate> templates = consentTemplateRepository.findAll();
        List<EstudianteConsent> toSave = new ArrayList<>();

        for (ConsentTemplate template : templates) {
            boolean exists = estudianteConsentRepository
                    .existsByEstudiante_IdAndConsentTemplate_Id(estudiante.getId(), template.getId());
            if (!exists) {
                EstudianteConsent consent = new EstudianteConsent();
                consent.setEstudiante(estudiante);
                consent.setConsentTemplate(template);
                consent.setAceptado(false);
                consent.setFechaAceptacion(null);
                toSave.add(consent);
            }
        }

        for (EstudianteConsent consent : estudianteConsentRepository.findByEstudiante(estudiante)) {
            boolean versionVigente = consent.getConsentTemplate().getVersion()
                    .equals(consent.getVersionAceptada());
            if (consent.isAceptado() && !versionVigente) {
                consent.setAceptado(false);
                toSave.add(consent);
            }
        }

        if (!toSave.isEmpty()) {
            estudianteConsentRepository.saveAll(toSave);
        }
    }

    @Transactional
    public List<EstudianteConsent> getConsentsByEstudiante(Long estudianteId) {
        Estudiante estudiante = findEstudiante(estudianteId);
        syncConsents(estudiante);
        return estudianteConsentRepository.findByEstudiante(estudiante);
    }

    // true si el estudiante tiene algún consentimiento sin aceptar (o con versión vieja)
    @Transactional
    public boolean hasPendingConsents(Long estudianteId) {
        Estudiante estudiante = findEstudiante(estudianteId);
        syncConsents(estudiante);
        return !estudianteConsentRepository.findByEstudianteAndAceptado(estudiante, false).isEmpty();
    }

    // Acepta un consentimiento SOLO si pertenece al estudiante indicado.
    // Deja constancia de fecha, hora y versión aceptada.
    @Transactional
    public EstudianteConsent acceptConsent(Long estudianteId, Long consentId) {
        EstudianteConsent consent = estudianteConsentRepository
                .findByIdAndEstudiante_Id(consentId, estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Consentimiento no encontrado"));
        consent.setAceptado(true);
        consent.setFechaAceptacion(LocalDateTime.now());
        consent.setVersionAceptada(consent.getConsentTemplate().getVersion());
        return estudianteConsentRepository.save(consent);
    }

    private Estudiante findEstudiante(Long estudianteId) {
        return estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
    }
}