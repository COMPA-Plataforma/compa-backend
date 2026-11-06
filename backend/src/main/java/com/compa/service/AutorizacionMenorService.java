package com.compa.service;

import com.compa.dto.request.RevisionAutorizacionRequest;
import com.compa.dto.response.AutorizacionMenorResponse;
import com.compa.dto.response.GuardianResponse;
import com.compa.enums.DecisionRevision;
import com.compa.enums.DocumentType;
import com.compa.enums.EstadoAutorizacionMenor;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.AutorizacionMenor;
import com.compa.model.AutorizacionMenorArchivo;
import com.compa.model.Estudiante;
import com.compa.model.Guardian;
import com.compa.model.Orientador;
import com.compa.repository.AutorizacionMenorArchivoRepository;
import com.compa.repository.AutorizacionMenorRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.GuardianRepository;
import com.compa.repository.OrientadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

// Autorización del representante legal de los estudiantes menores de edad (tarjeta de identidad):
// el estudiante descarga el documento, lo sube firmado y escaneado, y el psicoorientador lo revisa.
// Mientras no esté aprobada, el estudiante no puede usar el resto de la plataforma.
@Service
@RequiredArgsConstructor
public class AutorizacionMenorService {

    // Versión del texto del documento (debe coincidir con AUTORIZACION_VERSION del front)
    public static final String VERSION_DOCUMENTO = "1.0";
    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    private final AutorizacionMenorRepository autorizacionRepository;
    private final AutorizacionMenorArchivoRepository archivoRepository;
    private final EstudianteRepository estudianteRepository;
    private final GuardianRepository guardianRepository;
    private final OrientadorRepository orientadorRepository;

    // Resultado de descargar el documento
    public record Archivo(String nombre, String tipo, byte[] contenido) {}

    // ───────────── Acceso (lo usa el interceptor) ─────────────

    public static boolean esMenor(Estudiante e) {
        return e.getDocumentType() == DocumentType.TARJETA_DE_IDENTIDAD;
    }

    // true si el estudiante puede usar la plataforma: no es menor, o su autorización está aprobada.
    // Si es un menor que ya existía y nunca tuvo trámite, se crea pendiente en este momento.
    @Transactional
    public boolean accesoPermitido(String estudianteEmail) {
        Estudiante e = estudianteRepository.findByEmail(estudianteEmail).orElse(null);
        if (e == null || !esMenor(e)) {
            return true;
        }
        return obtenerOCrear(e).getEstado() == EstadoAutorizacionMenor.APROBADA;
    }

    // Se llama al registrar un menor: deja el trámite en PENDIENTE
    @Transactional
    public void crearPendiente(Estudiante estudiante) {
        if (esMenor(estudiante)) {
            obtenerOCrear(estudiante);
        }
    }

    // ───────────── Estudiante ─────────────

    @Transactional
    public AutorizacionMenorResponse obtenerPropia(String estudianteEmail) {
        Estudiante e = estudiantePropio(estudianteEmail);
        return toResponse(obtenerOCrear(e));
    }

    @Transactional
    public AutorizacionMenorResponse subir(String estudianteEmail, MultipartFile file) {
        Estudiante e = estudiantePropio(estudianteEmail);
        AutorizacionMenor a = obtenerOCrear(e);

        if (a.getEstado() == EstadoAutorizacionMenor.EN_REVISION) {
            throw new BusinessException("Ya enviaste el documento. Tu psicoorientador lo está revisando.");
        }
        if (a.getEstado() == EstadoAutorizacionMenor.APROBADA) {
            throw new BusinessException("Tu autorización ya fue aprobada.");
        }
        if (!guardianRepository.existsByEstudianteId(e.getId())) {
            throw new BusinessException(
                    "Tu psicoorientador aún no ha registrado los datos de tu acudiente. Pídele que los registre.");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Selecciona el documento escaneado.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BusinessException("El documento no puede superar los 5 MB.");
        }

        byte[] contenido;
        try {
            contenido = file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("No se pudo leer el documento. Inténtalo de nuevo.");
        }
        // El tipo se decide por el contenido del archivo, no por lo que diga el navegador
        String tipo = detectarTipo(contenido);
        if (tipo == null) {
            throw new BusinessException("El documento debe ser un PDF, JPG o PNG.");
        }

        AutorizacionMenorArchivo archivo = archivoRepository.findByAutorizacionId(a.getId())
                .orElseGet(() -> {
                    AutorizacionMenorArchivo nuevo = new AutorizacionMenorArchivo();
                    nuevo.setAutorizacion(a);
                    return nuevo;
                });
        archivo.setContenido(contenido);
        archivoRepository.save(archivo);

        a.setArchivoNombre(nombreSeguro(file.getOriginalFilename(), tipo));
        a.setArchivoTipo(tipo);
        a.setArchivoTamano((long) contenido.length);
        a.setSubidoEn(LocalDateTime.now(BOGOTA));
        a.setVersionDocumento(VERSION_DOCUMENTO);
        a.setEstado(EstadoAutorizacionMenor.EN_REVISION);
        // Una subida nueva borra el resultado de la revisión anterior
        a.setMotivoRechazo(null);
        a.setRevisadoPor(null);
        a.setRevisadoEn(null);
        return toResponse(autorizacionRepository.save(a));
    }

    // ───────────── Psicoorientador ─────────────

    @Transactional
    public AutorizacionMenorResponse obtenerDeEstudiante(Long estudianteId, String orientadorEmail) {
        Estudiante e = estudianteDelOrientador(estudianteId, orientadorEmail);
        if (!esMenor(e)) {
            throw new BusinessException("Este estudiante no es menor de edad: no requiere autorización.");
        }
        return toResponse(obtenerOCrear(e));
    }

    @Transactional(readOnly = true)
    public Archivo descargar(Long estudianteId, String orientadorEmail) {
        estudianteDelOrientador(estudianteId, orientadorEmail);
        AutorizacionMenor a = autorizacionRepository.findByEstudianteId(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aún no ha subido el documento."));
        AutorizacionMenorArchivo archivo = archivoRepository.findByAutorizacionId(a.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aún no ha subido el documento."));
        return new Archivo(a.getArchivoNombre(), a.getArchivoTipo(), archivo.getContenido());
    }

    @Transactional
    public AutorizacionMenorResponse revisar(Long estudianteId, String orientadorEmail,
                                             RevisionAutorizacionRequest request) {
        Estudiante e = estudianteDelOrientador(estudianteId, orientadorEmail);
        Orientador orientador = orientadorRepository.findByEmail(orientadorEmail)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede revisar el documento."));
        AutorizacionMenor a = autorizacionRepository.findByEstudianteId(e.getId())
                .orElseThrow(() -> new ResourceNotFoundException("El estudiante aún no ha subido el documento."));

        if (a.getEstado() != EstadoAutorizacionMenor.EN_REVISION) {
            throw new BusinessException("Solo se puede revisar un documento que el estudiante ya envió.");
        }

        if (request.getDecision() == DecisionRevision.RECHAZAR) {
            String motivo = request.getMotivo() == null ? "" : request.getMotivo().trim();
            if (motivo.isEmpty()) {
                throw new BusinessException("Escribe el motivo del rechazo para que el estudiante sepa qué corregir.");
            }
            a.setEstado(EstadoAutorizacionMenor.RECHAZADA);
            a.setMotivoRechazo(motivo);
        } else {
            a.setEstado(EstadoAutorizacionMenor.APROBADA);
            a.setMotivoRechazo(null);
            // La fecha de autorización del acudiente es el día en que se aprueba el documento
            guardianRepository.findByEstudianteId(e.getId()).ifPresent(g -> {
                g.setFechaAutorizacion(LocalDate.now(BOGOTA));
                guardianRepository.save(g);
            });
        }
        a.setRevisadoPor(orientador);
        a.setRevisadoEn(LocalDateTime.now(BOGOTA));
        return toResponse(autorizacionRepository.save(a));
    }

    // Documentos que esperan revisión, de los estudiantes del psicoorientador
    @Transactional(readOnly = true)
    public List<AutorizacionMenorResponse> pendientesDeRevision(String orientadorEmail) {
        return autorizacionRepository
                .findByEstadoAndEstudianteOrientadorEmailOrderBySubidoEnAsc(
                        EstadoAutorizacionMenor.EN_REVISION, orientadorEmail)
                .stream().map(this::toResponse).toList();
    }

    // ───────────── Internos ─────────────

    private AutorizacionMenor obtenerOCrear(Estudiante e) {
        return autorizacionRepository.findByEstudianteId(e.getId()).orElseGet(() -> {
            AutorizacionMenor nueva = new AutorizacionMenor();
            nueva.setEstudiante(e);
            nueva.setEstado(EstadoAutorizacionMenor.PENDIENTE);
            nueva.setVersionDocumento(VERSION_DOCUMENTO);
            return autorizacionRepository.save(nueva);
        });
    }

    private Estudiante estudiantePropio(String email) {
        Estudiante e = estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un estudiante puede ver su autorización."));
        if (!esMenor(e)) {
            throw new BusinessException("Tu cuenta no requiere autorización de un representante legal.");
        }
        return e;
    }

    // Solo el psicoorientador asignado al estudiante
    private Estudiante estudianteDelOrientador(Long estudianteId, String orientadorEmail) {
        Orientador o = orientadorRepository.findByEmail(orientadorEmail)
                .orElseThrow(() -> new ForbiddenException("Solo un psicoorientador puede revisar este documento."));
        Estudiante e = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante", estudianteId));
        if (e.getOrientador() == null || !e.getOrientador().getId().equals(o.getId())) {
            throw new ForbiddenException(
                    "Solo el psicoorientador asignado puede revisar el documento de este estudiante.");
        }
        return e;
    }

    // PDF, JPG o PNG según los primeros bytes del archivo
    private static String detectarTipo(byte[] b) {
        if (b.length >= 4 && b[0] == '%' && b[1] == 'P' && b[2] == 'D' && b[3] == 'F') {
            return "application/pdf";
        }
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length >= 4 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        return null;
    }

    // Nombre del archivo sin rutas ni caracteres raros
    private static String nombreSeguro(String original, String tipo) {
        String base = original == null ? "" : original;
        int i = Math.max(base.lastIndexOf('/'), base.lastIndexOf('\\'));
        base = base.substring(i + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (base.isEmpty()) {
            base = "autorizacion";
        }
        if (base.length() > 150) {
            base = base.substring(base.length() - 150);
        }
        return base;
    }

    private AutorizacionMenorResponse toResponse(AutorizacionMenor a) {
        Estudiante e = a.getEstudiante();
        Guardian g = guardianRepository.findByEstudianteId(e.getId()).orElse(null);
        Orientador por = a.getRevisadoPor();
        boolean estadoPermiteSubir = a.getEstado() == EstadoAutorizacionMenor.PENDIENTE
                || a.getEstado() == EstadoAutorizacionMenor.RECHAZADA;
        return AutorizacionMenorResponse.builder()
                .estudianteId(e.getId())
                .estudianteNombre(e.getName() + " " + e.getLastName())
                .estado(a.getEstado())
                .versionDocumento(a.getVersionDocumento())
                .acudiente(g != null ? toGuardianResponse(g, e.getId()) : null)
                .puedeSubir(estadoPermiteSubir && g != null)
                .archivoNombre(a.getArchivoNombre())
                .archivoTipo(a.getArchivoTipo())
                .archivoTamano(a.getArchivoTamano())
                .subidoEn(a.getSubidoEn())
                .revisadoPorNombre(por != null ? por.getName() + " " + por.getLastName() : null)
                .revisadoEn(a.getRevisadoEn())
                .motivoRechazo(a.getMotivoRechazo())
                .build();
    }

    private GuardianResponse toGuardianResponse(Guardian g, Long estudianteId) {
        GuardianResponse r = new GuardianResponse();
        r.setId(g.getId());
        r.setName(g.getName());
        r.setLastName(g.getLastName());
        r.setDocumentType(g.getDocumentType());
        r.setIdentityDocument(g.getIdentityDocument());
        r.setRelationship(g.getRelationship());
        r.setEmail(g.getEmail());
        r.setPhoneNumber(g.getPhoneNumber());
        r.setEstudianteId(estudianteId);
        r.setFechaAutorizacion(g.getFechaAutorizacion());
        return r;
    }
}