package com.compa.repository;

import com.compa.model.TaskCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCheckInRepository extends JpaRepository<TaskCheckIn, Long> {
    List<TaskCheckIn> findByCheckInId(Long checkInId);

    Optional<TaskCheckIn> findByCheckInIdAndTaskId(Long checkInId, Long taskId);

    // Reportes de actividades de un estudiante entre dos fechas (ambas incluidas)
    List<TaskCheckIn> findByCheckIn_Estudiante_IdAndCheckIn_CheckInDateBetween(
            Long estudianteId, LocalDate from, LocalDate to);
}