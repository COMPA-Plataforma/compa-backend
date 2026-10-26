package com.compa.repository;

import com.compa.model.ExperienciaActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ExperienciaActividadRepository extends JpaRepository<ExperienciaActividad, Long> {
 
    List<ExperienciaActividad> findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(Long estudianteId);
 
    List<ExperienciaActividad> findByEstudianteIdAndActividadIdOrderByFechaDescCreatedAtDescIdDesc(
            Long estudianteId, Long actividadId);
 
    Optional<ExperienciaActividad> findByIdAndEstudianteId(Long id, Long estudianteId);
}
 