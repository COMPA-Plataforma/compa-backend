package com.compa.repository;

import com.compa.model.Derivacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DerivacionRepository extends JpaRepository<Derivacion, Long> {

    Optional<Derivacion> findByAlertId(Long alertId);

    List<Derivacion> findByEstudianteIdOrderByFechaDescIdDesc(Long estudianteId);
}