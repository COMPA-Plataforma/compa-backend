package com.compa.repository;

import com.compa.model.Seguimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeguimientoRepository extends JpaRepository<Seguimiento, Long> {

    // Del más reciente al más antiguo; si hay dos con la misma fecha, el registrado después va primero
    List<Seguimiento> findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(Long estudianteId);

    // Busca un seguimiento solo si pertenece a ese estudiante
    Optional<Seguimiento> findByIdAndEstudianteId(Long id, Long estudianteId);
}