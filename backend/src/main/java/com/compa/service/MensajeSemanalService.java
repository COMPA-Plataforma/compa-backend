package com.compa.service;

import com.compa.enums.EstudianteStatus;
import com.compa.enums.MotivoConsulta;
import com.compa.model.Atencion;
import com.compa.model.EnvioMensajeSemanal;
import com.compa.model.Estudiante;
import com.compa.model.MensajeAcompanamiento;
import com.compa.repository.AtencionRepository;
import com.compa.repository.EnvioMensajeSemanalRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.MensajeAcompanamientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Envía a cada estudiante un mensaje de acompañamiento por correo, una vez por semana.
@Slf4j
@Service
@RequiredArgsConstructor
public class MensajeSemanalService {

    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");
    // Los mensajes salen los lunes desde las 8:00 a.m. (hora de Colombia)
    private static final int HORA_ENVIO = 8;

    private final EstudianteRepository estudianteRepository;
    private final AtencionRepository atencionRepository;
    private final MensajeAcompanamientoRepository mensajeRepository;
    private final EnvioMensajeSemanalRepository envioRepository;
    private final EmailService emailService;

    private final Random random = new Random();

    // El envío semanal está APAGADO por defecto, para que desplegar el código no le escriba a nadie.
    // Para encenderlo, definir la variable de entorno MENSAJE_SEMANAL_HABILITADO=true
    // (en Render: Environment; en local: archivo .env). Ver docs/mensaje-semanal.md.
    @Value("${MENSAJE_SEMANAL_HABILITADO:false}")
    private boolean habilitado;

    // Corre cada hora. Si un estudiante aún no recibió el mensaje de esta semana, se lo envía.
    // Así, si la app estaba apagada el lunes, el mensaje sale en cuanto vuelve a estar activa,
    // y nunca se envía dos veces en la misma semana.
    @Scheduled(cron = "0 0 * * * *", zone = "America/Bogota")
    public void enviarMensajesSemanales() {
        if (!habilitado) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now(ZONA_BOGOTA);
        if (ahora.getDayOfWeek() == DayOfWeek.MONDAY && ahora.getHour() < HORA_ENVIO) {
            return; // el lunes todavía no es la hora
        }
        LocalDate semana = ahora.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<MensajeAcompanamiento> banco = mensajeRepository.findByActivoTrue();
        if (banco.isEmpty()) {
            log.warn("No hay mensajes de acompanamiento activos; no se envia nada esta semana");
            return;
        }

        for (Estudiante estudiante : estudianteRepository.findByStatus(EstudianteStatus.ACTIVO)) {
            // Una cuenta que aún no se activó no recibe mensajes
            if (!estudiante.isAccountActivated()) {
                continue;
            }
            try {
                enviarSiCorresponde(estudiante, semana, banco);
            } catch (Exception e) {
                // Un fallo con un estudiante no debe impedir el envío a los demás
                log.error("No se pudo enviar el mensaje semanal a {}: {}", estudiante.getEmail(), e.getMessage());
            }
        }
    }

    private void enviarSiCorresponde(Estudiante estudiante, LocalDate semana, List<MensajeAcompanamiento> banco) {
        if (envioRepository.existsByEstudianteIdAndSemana(estudiante.getId(), semana)) {
            return;
        }

        MensajeAcompanamiento mensaje = elegirMensaje(estudiante, banco);

        emailService.sendNotificationEmail(
                estudiante.getEmail(),
                estudiante.getName(),
                "Tu mensaje de acompañamiento de la semana (prioridad " + mensaje.getPrioridad().getDisplayName() + ")",
                "Prioridad: " + mensaje.getPrioridad().getDisplayName() + "\n\n"
                        + mensaje.getTexto() + "\n\n"
                        + "Este mensaje es de acompañamiento y no reemplaza tu sesión con tu psicoorientador.");

        EnvioMensajeSemanal envio = new EnvioMensajeSemanal();
        envio.setEstudiante(estudiante);
        envio.setMensaje(mensaje);
        envio.setSemana(semana);
        envio.setEnviadoAt(LocalDateTime.now(ZONA_BOGOTA));
        envioRepository.save(envio);
        log.info("Mensaje semanal enviado a {} (prioridad {})", estudiante.getEmail(), mensaje.getPrioridad());
    }

    // Elige el mensaje de la semana. Se recorre el banco del estudiante por "vueltas": no recibe un
    // mensaje que ya recibió hasta haber recibido todos los de sus motivos (o todos los generales,
    // si no tiene motivos). Al completar una vuelta empieza otra, sin repetir el último mensaje.
    MensajeAcompanamiento elegirMensaje(Estudiante estudiante, List<MensajeAcompanamiento> banco) {
        Set<MotivoConsulta> motivos = atencionRepository.findByEstudianteId(estudiante.getId())
                .map(Atencion::getMotivos)
                .orElse(Set.of());

        // Mensajes que le corresponden: los de sus motivos; si no tiene (o no hay activos), los generales;
        // y como último recurso, todo el banco.
        List<MensajeAcompanamiento> pool = banco.stream()
                .filter(m -> motivos.isEmpty() ? m.getMotivo() == null : motivos.contains(m.getMotivo()))
                .toList();
        if (pool.isEmpty()) {
            pool = banco.stream().filter(m -> m.getMotivo() == null).toList();
        }
        if (pool.isEmpty()) {
            pool = banco;
        }

        List<Long> historial = envioRepository.findByEstudianteIdOrderBySemanaAsc(estudiante.getId()).stream()
                .map(e -> e.getMensaje().getId())
                .toList();

        Long elegido = elegirId(historial, pool.stream().map(MensajeAcompanamiento::getId).toList(), random);
        return pool.stream().filter(m -> m.getId().equals(elegido)).findFirst().orElseThrow();
    }

    /**
     * Lógica de la rotación, sobre ids. "historial" son los mensajes ya enviados (del más antiguo al más
     * reciente) y "pool" los que le corresponden ahora. Se reconstruye la vuelta actual: los mensajes del
     * pool recibidos desde la última vez que se completó el pool. Se elige al azar entre los que faltan
     * de esa vuelta, sin repetir nunca el último enviado.
     */
    static Long elegirId(List<Long> historial, List<Long> pool, Random random) {
        Set<Long> vistos = new HashSet<>();
        for (Long id : historial) {
            if (pool.contains(id)) {
                vistos.add(id);
                if (vistos.containsAll(pool)) {
                    vistos.clear(); // vuelta completa: empieza otra
                }
            }
        }
        Long ultimo = historial.isEmpty() ? null : historial.get(historial.size() - 1);

        List<Long> candidatos = pool.stream()
                .filter(id -> !vistos.contains(id) && !id.equals(ultimo))
                .toList();
        if (candidatos.isEmpty()) {
            candidatos = pool.stream().filter(id -> !id.equals(ultimo)).toList();
        }
        if (candidatos.isEmpty()) {
            candidatos = pool; // un solo mensaje disponible: no se puede evitar repetirlo
        }
        return candidatos.get(random.nextInt(candidatos.size()));
    }
}