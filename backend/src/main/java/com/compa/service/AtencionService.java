package com.compa.service;

import com.compa.dto.request.AtencionRequest;
import com.compa.dto.response.AtencionResponse;
import com.compa.dto.response.AtencionResponse.MotivoItem;
import com.compa.dto.response.AtencionVersionResponse;
import com.compa.enums.MotivoConsulta;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.Atencion;
import com.compa.model.AtencionVersion;
import com.compa.model.Estudiante;
import com.compa.model.Orientador;
import com.compa.repository.AtencionRepository;
import com.compa.repository.AtencionVersionRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AtencionService {

    private final AtencionRepository atencionRepository;
    private final AtencionVersionRepository versionRepository;
    private final EstudianteRepository estudianteRepository;
    private final OrientadorRepository orientadorRepository;

    // Registrar la primera atención. Si el estudiante aún no tiene orientador,
    // queda asignado a quien la registra.
    @Transactional
    public AtencionResponse registrar(Long estudianteId, String orientadorEmail, AtencionRequest request) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);

        if (estudiante.getOrientador() == null) {
            estudiante.setOrientador(orientador);
            estudianteRepository.save(estudiante);
        } else if (!estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException("El estudiante esta asignado a otro psicoorientador.");
        }

        if (atencionRepository.existsByEstudianteId(estudianteId)) {
            throw new BusinessException("El estudiante ya tiene una atencion registrada. Actualicela en su lugar.");
        }
        if (request.getEntrevistaRecepcion() == null || request.getEntrevistaRecepcion().isBlank()) {
            throw new BusinessException("La entrevista de recepcion es obligatoria al registrar la atencion.");
        }

        Atencion atencion = new Atencion();
        atencion.setEstudiante(estudiante);
        aplicar(atencion, request);
        atencion = atencionRepository.save(atencion);

        AtencionVersion version = guardarVersion(atencion, orientador);
        return toResponse(atencion, version.getNumero());
    }

    // Completar o actualizar la atención en cualquier sesión.
    @Transactional
    public AtencionResponse actualizar(Long estudianteId, String orientadorEmail, AtencionRequest request) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        verificarAcceso(estudiante, orientador);

        Atencion atencion = atencionRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aun no tiene atencion registrada"));

        List<Object> antes = valores(atencion);

        aplicar(atencion, request);

        if (antes.equals(valores(atencion))) {
            throw new BusinessException("No hay cambios para guardar.");
        }

        atencion = atencionRepository.save(atencion);
        AtencionVersion version = guardarVersion(atencion, orientador);
        return toResponse(atencion, version.getNumero());
    }

    @Transactional(readOnly = true)
    public AtencionResponse consultar(Long estudianteId, String orientadorEmail) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        sinAtencionSiNoTieneOrientador(estudiante);
        verificarAcceso(estudiante, orientador);

        Atencion atencion = atencionRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aun no tiene atencion registrada"));
        return toResponse(atencion, versionRepository.countByAtencionId(atencion.getId()));
    }

    @Transactional(readOnly = true)
    public List<AtencionVersionResponse> historial(Long estudianteId, String orientadorEmail) {
        Orientador orientador = orientadorActual(orientadorEmail);
        Estudiante estudiante = estudiante(estudianteId);
        sinAtencionSiNoTieneOrientador(estudiante);
        verificarAcceso(estudiante, orientador);

        Atencion atencion = atencionRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aun no tiene atencion registrada"));
        return versionRepository.findByAtencionIdOrderByNumeroDesc(atencion.getId()).stream()
                .map(this::toVersionResponse)
                .toList();
    }

    public List<MotivoItem> catalogoMotivos() {
        return Arrays.stream(MotivoConsulta.values())
                .map(m -> new MotivoItem(m.name(), m.getDisplayName()))
                .toList();
    }

    // ---------- helpers ----------

    private Orientador orientadorActual(String email) {
        return orientadorRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede gestionar la atencion."));
    }

    private Estudiante estudiante(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", id));
    }

    // Un estudiante sin orientador aún no tiene atención (se asigna al registrar la primera):
    // se responde "no encontrada" y no "prohibido", para que la pantalla ofrezca registrarla.
    private void sinAtencionSiNoTieneOrientador(Estudiante estudiante) {
        if (estudiante.getOrientador() == null) {
            throw new ResourceNotFoundException("El estudiante aun no tiene atencion registrada");
        }
    }

    private void verificarAcceso(Estudiante estudiante, Orientador orientador) {
        if (estudiante.getOrientador() == null
                || !estudiante.getOrientador().getId().equals(orientador.getId())) {
            throw new ForbiddenException("Solo el psicoorientador asignado puede acceder a la atencion de este estudiante.");
        }
    }

    // Aplica solo los campos enviados (null = sin cambio, vacío = borrar).
    private void aplicar(Atencion atencion, AtencionRequest r) {
        if (r.getEntrevistaRecepcion() != null) {
            if (r.getEntrevistaRecepcion().isBlank()) {
                throw new BusinessException("La entrevista de recepcion no puede quedar vacia.");
            }
            atencion.setEntrevistaRecepcion(r.getEntrevistaRecepcion().trim());
        }
        if (r.getMotivos() != null) {
            atencion.getMotivos().clear();
            atencion.getMotivos().addAll(r.getMotivos());
        }
        if (r.getMotivoOtro() != null) {
            atencion.setMotivoOtro(r.getMotivoOtro().isBlank() ? null : r.getMotivoOtro().trim());
        }
        atencion.setSituacionActual(texto(r.getSituacionActual(), atencion.getSituacionActual()));
        atencion.setAntecedentes(texto(r.getAntecedentes(), atencion.getAntecedentes()));
        atencion.setAspectosPsicosociales(texto(r.getAspectosPsicosociales(), atencion.getAspectosPsicosociales()));
        atencion.setRelacionesFamiliares(texto(r.getRelacionesFamiliares(), atencion.getRelacionesFamiliares()));
        atencion.setObservacionesSalud(texto(r.getObservacionesSalud(), atencion.getObservacionesSalud()));
        atencion.setImpresionDiagnostica(texto(r.getImpresionDiagnostica(), atencion.getImpresionDiagnostica()));
        aplicarEps(atencion, r);
    }

    // Afiliación a EPS: si no está afiliado se borra el nombre; si está afiliado, el nombre es obligatorio.
    private void aplicarEps(Atencion atencion, AtencionRequest r) {
        if (r.getAfiliadoEps() != null) {
            atencion.setAfiliadoEps(r.getAfiliadoEps());
        }
        if (r.getEpsNombre() != null) {
            atencion.setEpsNombre(r.getEpsNombre().isBlank() ? null : r.getEpsNombre().trim());
        }
        if (Boolean.FALSE.equals(atencion.getAfiliadoEps())) {
            atencion.setEpsNombre(null);
        } else if (Boolean.TRUE.equals(atencion.getAfiliadoEps()) && atencion.getEpsNombre() == null) {
            throw new BusinessException("Indica a cual EPS esta afiliado el estudiante.");
        } else if (atencion.getAfiliadoEps() == null && atencion.getEpsNombre() != null) {
            throw new BusinessException("Para indicar la EPS marca que el estudiante esta afiliado.");
        }
    }

    // Campo de texto libre: null = sin cambio, vacío = borrar, otro = texto recortado.
    private String texto(String nuevo, String actual) {
        if (nuevo == null) return actual;
        return nuevo.isBlank() ? null : nuevo.trim();
    }

    // Valores de la atención, para saber si una actualización realmente cambió algo.
    private List<Object> valores(Atencion a) {
        return java.util.Arrays.asList(
                a.getEntrevistaRecepcion(), Set.copyOf(a.getMotivos()), a.getMotivoOtro(),
                a.getSituacionActual(), a.getAntecedentes(), a.getAspectosPsicosociales(),
                a.getRelacionesFamiliares(), a.getObservacionesSalud(), a.getImpresionDiagnostica(),
                a.getAfiliadoEps(), a.getEpsNombre());
    }

    private AtencionVersion guardarVersion(Atencion atencion, Orientador autor) {
        AtencionVersion v = new AtencionVersion();
        v.setAtencion(atencion);
        v.setNumero(versionRepository.countByAtencionId(atencion.getId()) + 1);
        v.setAutor(autor);
        v.setEntrevistaRecepcion(atencion.getEntrevistaRecepcion());
        v.setSituacionActual(atencion.getSituacionActual());
        v.setAntecedentes(atencion.getAntecedentes());
        v.setAspectosPsicosociales(atencion.getAspectosPsicosociales());
        v.setRelacionesFamiliares(atencion.getRelacionesFamiliares());
        v.setObservacionesSalud(atencion.getObservacionesSalud());
        v.setAfiliadoEps(atencion.getAfiliadoEps());
        v.setEpsNombre(atencion.getEpsNombre());
        v.setMotivos(atencion.getMotivos().stream()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(Enum::name)
                .collect(Collectors.joining(",")));
        v.setMotivoOtro(atencion.getMotivoOtro());
        v.setImpresionDiagnostica(atencion.getImpresionDiagnostica());
        return versionRepository.save(v);
    }

    private List<MotivoItem> items(Set<MotivoConsulta> motivos) {
        return new TreeSet<>(motivos).stream()
                .map(m -> new MotivoItem(m.name(), m.getDisplayName()))
                .toList();
    }

    private AtencionResponse toResponse(Atencion a, int version) {
        Orientador o = a.getEstudiante().getOrientador();
        return AtencionResponse.builder()
                .id(a.getId())
                .estudianteId(a.getEstudiante().getId())
                .orientadorId(o != null ? o.getId() : null)
                .orientadorNombre(o != null ? o.getName() + " " + o.getLastName() : null)
                .entrevistaRecepcion(a.getEntrevistaRecepcion())
                .situacionActual(a.getSituacionActual())
                .antecedentes(a.getAntecedentes())
                .aspectosPsicosociales(a.getAspectosPsicosociales())
                .relacionesFamiliares(a.getRelacionesFamiliares())
                .observacionesSalud(a.getObservacionesSalud())
                .afiliadoEps(a.getAfiliadoEps())
                .epsNombre(a.getEpsNombre())
                .motivos(items(a.getMotivos()))
                .motivoOtro(a.getMotivoOtro())
                .impresionDiagnostica(a.getImpresionDiagnostica())
                .version(version)
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }

    private AtencionVersionResponse toVersionResponse(AtencionVersion v) {
        Set<MotivoConsulta> motivos = new TreeSet<>();
        if (v.getMotivos() != null && !v.getMotivos().isBlank()) {
            Arrays.stream(v.getMotivos().split(",")).map(MotivoConsulta::valueOf).forEach(motivos::add);
        }
        return AtencionVersionResponse.builder()
                .numero(v.getNumero())
                .fecha(v.getFecha())
                .autorId(v.getAutor().getId())
                .autorNombre(v.getAutor().getName() + " " + v.getAutor().getLastName())
                .entrevistaRecepcion(v.getEntrevistaRecepcion())
                .situacionActual(v.getSituacionActual())
                .antecedentes(v.getAntecedentes())
                .aspectosPsicosociales(v.getAspectosPsicosociales())
                .relacionesFamiliares(v.getRelacionesFamiliares())
                .observacionesSalud(v.getObservacionesSalud())
                .afiliadoEps(v.getAfiliadoEps())
                .epsNombre(v.getEpsNombre())
                .motivos(items(motivos))
                .motivoOtro(v.getMotivoOtro())
                .impresionDiagnostica(v.getImpresionDiagnostica())
                .build();
    }
}