package com.compa.repository;

import com.compa.model.NovedadDerivacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NovedadDerivacionRepository extends JpaRepository<NovedadDerivacion, Long> {

    // Novedades en orden cronologico
    List<NovedadDerivacion> findByDerivacionIdOrderByFechaAscIdAsc(Long derivacionId);

    // La mas reciente define el estado actual de la derivacion
    Optional<NovedadDerivacion> findFirstByDerivacionIdOrderByFechaDescIdDesc(Long derivacionId);
}