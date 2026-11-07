package com.compa.service;

import com.compa.model.Estudiante;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.model.RecordatorioTarea;
import com.compa.repository.DailyCheckInRepository;
import com.compa.repository.EstudianteRepository;
import com.compa.repository.HabitPlanRepository;
import com.compa.repository.HabitTaskRepository;
import com.compa.repository.RecordatorioTareaRepository;
import com.compa.repository.TaskCheckInRepository;
import com.compa.enums.EstudianteStatus;
import com.compa.enums.NotificationChannel;
import com.compa.enums.NotificationType;
import com.compa.enums.PlanStatus;
import com.compa.model.*;
import com.compa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecordatorioService {
    private static final ZoneId ZONA_BOGOTA = ZoneId.of("America/Bogota");

    private final EstudianteRepository estudianteRepository;
    private final HabitPlanRepository habitPlanRepository;
    private final HabitTaskRepository habitTaskRepository;
    private final DailyCheckInRepository dailyCheckInRepository;
    private final TaskCheckInRepository taskCheckInRepository;
    private final RecordatorioTareaRepository recordatorioTareaRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    // Corre cada 15 minutos: envía el primer recordatorio a quien le toque en esta
    // ventana
    @Scheduled(cron = "0 0/15 * * * *")
    public void enviarRecordatoriosPendientes() {
        LocalDateTime now = LocalDateTime.now(ZONA_BOGOTA);
        LocalTime windowStart = now.toLocalTime().withSecond(0).withNano(0);
        LocalTime windowEnd = windowStart.plusMinutes(15);
        LocalDate today = now.toLocalDate();

        List<Estudiante> estudiantes = estudianteRepository
                .findByReminderTimeBetweenAndStatus(windowStart, windowEnd, EstudianteStatus.ACTIVO);

        for (Estudiante estudiante : estudiantes) {
            // si configuró días específicos y hoy no toca, se salta
            if (!ConfiguracionRecordatorioService.tocaHoy(estudiante.getReminderDays(), today)) {
                continue;
            }
            habitPlanRepository.findByEstudianteIdAndStatus(estudiante.getId(), PlanStatus.ACTIVO)
                    .ifPresent(activePlan -> procesarPrimerRecordatorio(estudiante, activePlan, today));
        }
    }

    private void procesarPrimerRecordatorio(Estudiante estudiante, HabitPlan activePlan, LocalDate today) {
        List<HabitTask> allTasks = habitTaskRepository.findByHabitPlanId(activePlan.getId());
        List<HabitTask> pendingTasks = tareasPendientesHoy(estudiante.getId(), today, allTasks);

        if (pendingTasks.isEmpty()) {
            return; // ya marcó todas las tareas del día, no se envía notificación
        }

        List<String> nombres = pendingTasks.stream().map(HabitTask::getName).collect(Collectors.toList());
        emailService.sendTaskReminderEmail(estudiante.getEmail(), estudiante.getName(), nombres, false);
        avisarEnCampana(estudiante, nombres, false);

        for (HabitTask task : pendingTasks) {
            if (recordatorioTareaRepository.findByHabitTaskIdAndFecha(task.getId(), today).isEmpty()) {
                RecordatorioTarea r = new RecordatorioTarea();
                r.setHabitTask(task);
                r.setEstudiante(estudiante);
                r.setFecha(today);
                r.setRecordatoriosEnviados(1);
                recordatorioTareaRepository.save(r);
            }
        }
        log.info("Recordatorio enviado a {} ({} tareas pendientes)", estudiante.getEmail(), pendingTasks.size());
    }

    // Corre cada hora: si pasaron 24h desde el primer recordatorio y la tarea sigue
    // sin registro, envía el segundo
    @Scheduled(cron = "0 0 * * * *")
    public void enviarRecordatoriosSeguimiento() {
        LocalDateTime limite = LocalDateTime.now(ZONA_BOGOTA).minusHours(24);
        List<RecordatorioTarea> pendientesSeguimiento = recordatorioTareaRepository
                .findByRecordatoriosEnviadosAndPrimerEnvioBefore(1, limite);

        Map<Long, List<RecordatorioTarea>> porEstudiante = pendientesSeguimiento.stream()
                .collect(Collectors.groupingBy(r -> r.getEstudiante().getId()));

        for (List<RecordatorioTarea> recordatorios : porEstudiante.values()) {
            Estudiante estudiante = recordatorios.get(0).getEstudiante();
            LocalDate fecha = recordatorios.get(0).getFecha();

            Set<Long> tareasYaRegistradas = tareasRegistradasEnFecha(estudiante.getId(), fecha);

            List<RecordatorioTarea> aunPendientes = recordatorios.stream()
                    .filter(r -> !tareasYaRegistradas.contains(r.getHabitTask().getId()))
                    .collect(Collectors.toList());

            if (aunPendientes.isEmpty()) {
                continue; // ya las marcó todas, no hace falta el seguimiento
            }

            List<String> nombres = aunPendientes.stream()
                    .map(r -> r.getHabitTask().getName()).collect(Collectors.toList());
            emailService.sendTaskReminderEmail(estudiante.getEmail(), estudiante.getName(), nombres, true);
            avisarEnCampana(estudiante, nombres, true);

            for (RecordatorioTarea r : aunPendientes) {
                r.setRecordatoriosEnviados(2);
                r.setSegundoEnvio(LocalDateTime.now());
                recordatorioTareaRepository.save(r);
            }
            log.info("Recordatorio de seguimiento enviado a {} ({} tareas)", estudiante.getEmail(),
                    aunPendientes.size());
        }
    }

    // Deja el aviso en la campana además del correo. Si falla, no tumba el envío
    private void avisarEnCampana(Estudiante estudiante, List<String> nombres, boolean seguimiento) {
        try {
            String titulo = seguimiento
                    ? "Aún te faltan actividades por reportar"
                    : "Tienes actividades por reportar";
            String mensaje = "Pendientes de hoy: " + String.join(", ", nombres) + ".";
            notificationService.createAndSend(
                    estudiante.getId(),
                    NotificationType.RECORDATORIO_CHECKIN,
                    NotificationChannel.IN_APP,
                    titulo,
                    mensaje);
        } catch (Exception e) {
            log.error("No se pudo crear la notificación para {}: {}", estudiante.getEmail(), e.getMessage());
        }
    }

    private List<HabitTask> tareasPendientesHoy(Long estudianteId, LocalDate today, List<HabitTask> allTasks) {
        Set<Long> registradas = tareasRegistradasEnFecha(estudianteId, today);
        return allTasks.stream()
                .filter(t -> !registradas.contains(t.getId()))
                .collect(Collectors.toList());
    }

    private Set<Long> tareasRegistradasEnFecha(Long estudianteId, LocalDate fecha) {
        return dailyCheckInRepository.findByEstudianteIdAndCheckInDate(estudianteId, fecha)
                .map(checkIn -> taskCheckInRepository.findByCheckInId(checkIn.getId()).stream()
                        .map(tc -> tc.getTask().getId())
                        .collect(Collectors.toSet()))
                .orElse(Set.of());
    }

}