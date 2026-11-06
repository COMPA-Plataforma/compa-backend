package com.compa.repository;

import com.compa.enums.EstadoAutorizacionMenor;
import com.compa.model.AutorizacionMenor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorizacionMenorRepository extends JpaRepository<AutorizacionMenor, Long> {

    Optional<AutorizacionMenor> findByEstudianteId(Long estudianteId);

    // Autorizaciones de un estado que pertenecen a estudiantes del psicoorientador con ese correo
    List<AutorizacionMenor> findByEstadoAndEstudianteOrientadorEmailOrderBySubidoEnAsc(
            EstadoAutorizacionMenor estado, String orientadorEmail);
}
