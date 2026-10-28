package com.compa.repository;

import com.compa.model.TaskCheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCheckInRepository extends JpaRepository<TaskCheckIn, Long> {
    List<TaskCheckIn> findByCheckInId(Long checkInId);

    Optional<TaskCheckIn> findByCheckInIdAndTaskId(Long checkInId, Long taskId);

    // Reportes de un estudiante en un rango de fechas (progreso del plan)
    List<TaskCheckIn> findByCheckIn_Estudiante_IdAndCheckIn_CheckInDateBetween(
            Long estudianteId, LocalDate from, LocalDate to);

    // Registros de un estudiante desde una fecha (inclusive), el más reciente primero
    @Query("select t from TaskCheckIn t join fetch t.checkIn c join fetch t.task k " +
           "where c.estudiante.id = :estudianteId and c.checkInDate >= :desde " +
           "order by c.checkInDate desc, k.id asc")
    List<TaskCheckIn> findDelEstudianteDesde(@Param("estudianteId") Long estudianteId,
                                             @Param("desde") LocalDate desde);

    // Todos los registros de un estudiante
    @Query("select t from TaskCheckIn t join fetch t.checkIn c join fetch t.task k " +
           "where c.estudiante.id = :estudianteId " +
           "order by c.checkInDate desc, k.id asc")
    List<TaskCheckIn> findDelEstudiante(@Param("estudianteId") Long estudianteId);

    // Busca un registro solo si pertenece a ese estudiante
    @Query("select t from TaskCheckIn t join fetch t.checkIn c join fetch t.task k " +
           "where t.id = :id and c.estudiante.id = :estudianteId")
    Optional<TaskCheckIn> findByIdAndEstudianteId(@Param("id") Long id,
                                                  @Param("estudianteId") Long estudianteId);
}