package com.compa.repository;

import com.compa.enums.EstudianteStatus;
import com.compa.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.time.LocalTime;

@Repository
public interface EstudianteRepository extends JpaRepository<Estudiante, Long> {
    // Spring Data JPA crea la consulta automáticamente
    boolean existsByIdentityDocument(String identityDocument);

    // Verificar si ya existe un estudiante con ese email
    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Estudiante> findByIdentityDocument(String identityDocument);

    // Buscar estudiante por token de activación
    Optional<Estudiante> findByActivationToken(String activationToken);

    // Buscar por nombre (ignorando mayúsculas) o documento
    List<Estudiante> findByNameContainingIgnoreCaseOrIdentityDocumentContaining(String name, String identityDocument);

    // Filtrar por estado (Activo/Inactivo)
    List<Estudiante> findByStatus(EstudianteStatus status);

    // Listar estudiantes vinculados a un orientador específico
    List<Estudiante> findByOrientadorId(Long orientadorId);

    // Estudiantes cuya hora de recordatorio cae en la ventana actual
    List<Estudiante> findByReminderTimeBetweenAndStatus(LocalTime start, LocalTime end, EstudianteStatus status);

    Optional<Estudiante> findByEmail(String email);

    // ¿Este estudiante está asignado a ese psicoorientador?
    boolean existsByIdAndOrientador_Id(Long id, Long orientadorId);

    // Filtrar por condición médica
    @Query("SELECT p FROM Estudiante p JOIN p.clinicalInfo c WHERE LOWER(c.mainCondition) LIKE LOWER(concat('%', :condition, '%'))")
    List<Estudiante> findByPrimaryCondition(@Param("condition") String condition);
}
