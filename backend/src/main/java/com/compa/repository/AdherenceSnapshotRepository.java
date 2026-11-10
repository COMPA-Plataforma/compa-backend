package com.compa.repository;

import com.compa.model.AdherenceSnapshot;
import com.compa.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AdherenceSnapshotRepository extends JpaRepository<AdherenceSnapshot, Long>

{
    Optional<AdherenceSnapshot> findByEstudianteAndSnapshotDate(Estudiante estudiante, LocalDate date);

    List<AdherenceSnapshot> findByEstudianteOrderBySnapshotDateDesc(Estudiante estudiante);

    // Foto de adherencia mas reciente de un estudiante hasta una fecha (para el informe agregado)
    Optional<AdherenceSnapshot> findFirstByEstudiante_IdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(
            Long estudianteId, LocalDate fecha);
}