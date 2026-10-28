package com.compa.service;

import com.compa.dto.request.ValidacionActividadRequest;
import com.compa.dto.response.ActividadInformadaResponse;
import com.compa.dto.response.ValidacionesResponse;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.model.Seguimiento;
import com.compa.model.TaskCheckIn;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import com.compa.repository.SeguimientoRepository;
import com.compa.repository.TaskCheckInRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

// El psicoorientador confirma en la sesion si el estudiante realmente hizo las actividades que informo.
@Service
@RequiredArgsConstructor
public class ValidacionActividadService {

    private final TaskCheckInRepository taskCheckInRepository;
    private final SeguimientoRepository seguimientoRepository;
    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    // Actividades informadas (cumplidas o no) desde la ultima sesion, o desde la fecha indicada.
    // Si todavia no hay ninguna sesion registrada ni filtro, se listan todas.
    @Transactional(readOnly = true)
    public ValidacionesResponse listar(Long estudianteId, String orientadorEmail, LocalDate desdeParam) {
        Orientador orientador = obtenerOrientador(orientadorEmail);
        Estudiante estudiante = obtenerEstudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        LocalDate ultimaSesion = seguimientoRepository
                .findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(estudianteId).stream()
                .filter(sg -> !sg.isAnulado())
                .map(Seguimiento::getFecha)
                .findFirst().orElse(null);

        LocalDate desde = desdeParam != null ? desdeParam : ultimaSesion;
        List<TaskCheckIn> registros = desde != null
                ? taskCheckInRepository.findDelEstudianteDesde(estudianteId, desde)
                : taskCheckInRepository.findDelEstudiante(estudianteId);

        return ValidacionesResponse.builder()
                .ultimaSesion(ultimaSesion)
                .desde(desde)
                .actividades(registros.stream().map(this::toResponse).toList())
                .build();
    }

    // Marca el registro como validado o no realizado. Queda guardado quien y cuando.
    @Transactional
    public ActividadInformadaResponse validar(Long estudianteId, Long registroId, String orientadorEmail,
            ValidacionActividadRequest request) {
        Orientador orientador = obtenerOrientador(orientadorEmail);
        Estudiante estudiante = obtenerEstudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        TaskCheckIn registro = taskCheckInRepository.findByIdAndEstudianteId(registroId, estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Registro de actividad", registroId));

        registro.setEstadoValidacion(request.getEstado());
        registro.setValidadaEn(LocalDateTime.now(ZoneId.of("America/Bogota")));
        registro.setValidadaPor(orientador);
        return toResponse(taskCheckInRepository.save(registro));
    }

    private Orientador obtenerOrientador(String email) {
        return orientadorRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException(
                        "Solo un psicoorientador puede validar las actividades de un estudiante."));
    }

    private Estudiante obtenerEstudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
    }

    // Solo el psicoorientador asignado al estudiante puede validar sus actividades.
    private void verificarAcceso(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() == null) {
            throw new ForbiddenException(
                    "El estudiante aun no tiene psicoorientador asignado. Se asigna al registrar su atencion.");
        }
        if (!estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException(
                    "Solo el psicoorientador asignado puede validar las actividades de este estudiante.");
        }
    }

    private ActividadInformadaResponse toResponse(TaskCheckIn t) {
        Orientador por = t.getValidadaPor();
        return ActividadInformadaResponse.builder()
                .id(t.getId())
                .actividadId(t.getTask().getId())
                .actividadNombre(t.getTask().getName())
                .fecha(t.getCheckIn().getCheckInDate())
                .informadaCumplida(t.isCompleted())
                .barrera(t.getBarrier() != null ? t.getBarrier().name() : null)
                .barreraEtiqueta(t.getBarrier() != null ? t.getBarrier().getDisplayName() : null)
                .comentario(t.getComment())
                .estadoValidacion(t.getEstadoValidacion())
                .validadaEn(t.getValidadaEn())
                .validadaPorNombre(por != null ? por.getName() + " " + por.getLastName() : null)
                .build();
    }
}