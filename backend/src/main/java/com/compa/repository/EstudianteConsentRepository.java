package com.compa.repository;

import com.compa.model.Estudiante;
import com.compa.model.EstudianteConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstudianteConsentRepository extends JpaRepository<EstudianteConsent, Long> {

    List<EstudianteConsent> findByEstudiante(Estudiante estudiante);

    List<EstudianteConsent> findByEstudianteAndAceptado(Estudiante estudiante, boolean aceptado);

    // Busca un consentimiento solo si pertenece a ese estudiante (control de propiedad)
    Optional<EstudianteConsent> findByIdAndEstudiante_Id(Long id, Long estudianteId);

    // ¿El estudiante ya tiene una fila para esta plantilla?
    boolean existsByEstudiante_IdAndConsentTemplate_Id(Long estudianteId, Long consentTemplateId);
}