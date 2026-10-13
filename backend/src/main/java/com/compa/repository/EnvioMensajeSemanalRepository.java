package com.compa.repository;

import com.compa.model.EnvioMensajeSemanal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EnvioMensajeSemanalRepository extends JpaRepository<EnvioMensajeSemanal, Long> {
    // ¿Ya se le envió el mensaje de esta semana?
    boolean existsByEstudianteIdAndSemana(Long estudianteId, LocalDate semana);

    // Todos los envíos del estudiante, del más antiguo al más reciente (para saber qué mensajes ya recibió)
    List<EnvioMensajeSemanal> findByEstudianteIdOrderBySemanaAsc(Long estudianteId);
}