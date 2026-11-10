package com.compa.repository;

import com.compa.model.Atencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtencionRepository extends JpaRepository<Atencion, Long> {
    Optional<Atencion> findByEstudianteId(Long estudianteId);

    boolean existsByEstudianteId(Long estudianteId);

    // Atenciones registradas en un rango [desde, hasta) para el informe agregado
    List<Atencion> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime desde, LocalDateTime hasta);
}