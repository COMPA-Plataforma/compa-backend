package com.compa.service;

import com.compa.dto.request.SeguimientoRequest;
import com.compa.dto.response.SeguimientoResponse;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.model.Seguimiento;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import com.compa.repository.SeguimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeguimientoService {

    private final SeguimientoRepository seguimientoRepository;
    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    // Registrar un seguimiento. Siempre crea uno nuevo: nunca sobrescribe los anteriores.
    @Transactional
    public SeguimientoResponse registrar(Long estudianteId, String orientadorEmail, SeguimientoRequest request) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        LocalDate hoy = LocalDate.now(ZoneId.of("America/Bogota"));
        if (request.getFecha().isAfter(hoy)) {
            throw new BusinessException("La fecha del seguimiento no puede ser futura");
        }

        Seguimiento s = new Seguimiento();
        s.setEstudiante(estudiante);
        s.setAutor(orientador);
        s.setFecha(request.getFecha());
        s.setDescripcion(request.getDescripcion().trim());
        s.setFaltoCita(request.isFaltoCita());
        s.setFaltoPrimeraCita(request.isFaltoPrimeraCita());
        s.setCanceladaEstudiante(request.isCanceladaEstudiante());
        s.setCanceladaDepartamento(request.isCanceladaDepartamento());
        s.setCitaVirtual(request.isCitaVirtual());
        s.setPrimeraVez(request.isPrimeraVez());
        s.setCierreProceso(request.isCierreProceso());
        s.setRemision(request.isRemision());
        return toResponse(seguimientoRepository.save(s));
    }

    // Historial del más reciente al más antiguo, incluidos los anulados (marcados como tales).
    @Transactional(readOnly = true)
    public List<SeguimientoResponse> historial(Long estudianteId, String orientadorEmail) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        return seguimientoRepository.findByEstudianteIdOrderByFechaDescCreatedAtDescIdDesc(estudianteId)
                .stream().map(this::toResponse).toList();
    }

    // "Eliminar" = anular: se marca y se conserva, no se borra de la base de datos.
    @Transactional
    public SeguimientoResponse anular(Long estudianteId, Long seguimientoId, String orientadorEmail) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        Seguimiento s = seguimientoRepository.findByIdAndEstudianteId(seguimientoId, estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Seguimiento", seguimientoId));
        if (s.isAnulado()) {
            throw new BusinessException("El seguimiento ya esta anulado");
        }
        s.setAnulado(true);
        s.setAnuladoAt(LocalDateTime.now());
        s.setAnuladoPor(orientador);
        return toResponse(seguimientoRepository.save(s));
    }

    // ---------- helpers ----------

    private Orientador orientadorActual(String email) {
        return orientadorRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede gestionar los seguimientos."));
    }

    private Estudiante estudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
    }

    // Solo el psicoorientador asignado al estudiante puede registrar y consultar sus seguimientos.
    private void verificarAcceso(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() == null) {
            throw new ForbiddenException(
                    "El estudiante aun no tiene psicoorientador asignado. Se asigna al registrar su atencion.");
        }
        if (!estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException(
                    "Solo el psicoorientador asignado puede acceder a los seguimientos de este estudiante.");
        }
    }

    private SeguimientoResponse toResponse(Seguimiento s) {
        Orientador a = s.getAutor();
        Orientador an = s.getAnuladoPor();
        return SeguimientoResponse.builder()
                .id(s.getId())
                .estudianteId(s.getEstudiante().getId())
                .fecha(s.getFecha())
                .descripcion(s.getDescripcion())
                .faltoCita(s.isFaltoCita())
                .faltoPrimeraCita(s.isFaltoPrimeraCita())
                .canceladaEstudiante(s.isCanceladaEstudiante())
                .canceladaDepartamento(s.isCanceladaDepartamento())
                .citaVirtual(s.isCitaVirtual())
                .primeraVez(s.isPrimeraVez())
                .cierreProceso(s.isCierreProceso())
                .remision(s.isRemision())
                .autorId(a.getId())
                .autorNombre(a.getName() + " " + a.getLastName())
                .anulado(s.isAnulado())
                .anuladoAt(s.getAnuladoAt())
                .anuladoPorNombre(an != null ? an.getName() + " " + an.getLastName() : null)
                .createdAt(s.getCreatedAt())
                .build();
    }
}