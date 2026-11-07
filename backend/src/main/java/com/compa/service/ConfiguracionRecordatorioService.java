package com.compa.service;

import com.compa.dto.request.ConfiguracionRecordatorioRequest;
import com.compa.dto.response.ConfiguracionRecordatorioResponse;
import com.compa.enums.FrecuenciaRecordatorio;
import com.compa.exception.BusinessException;
import com.compa.exception.ForbiddenException;
import com.compa.model.Estudiante;
import com.compa.repository.EstudianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Hora y días en que el estudiante recibe sus recordatorios. Solo puede ver y cambiar la suya:
// siempre se busca por el correo de quien hace la petición, nunca por un id que llegue en la ruta.
// El envío lo hace RecordatorioService cada 15 minutos leyendo esta configuración, así que un cambio
// aplica desde el siguiente ciclo.
@Service
@RequiredArgsConstructor
public class ConfiguracionRecordatorioService {

    // Hora predeterminada del sistema (la misma de Estudiante.reminderTime)
    public static final LocalTime HORA_PREDETERMINADA = LocalTime.of(20, 0);

    private final EstudianteRepository estudianteRepository;

    @Transactional(readOnly = true)
    public ConfiguracionRecordatorioResponse obtener(String estudianteEmail) {
        return toResponse(estudiante(estudianteEmail));
    }

    @Transactional
    public ConfiguracionRecordatorioResponse guardar(String estudianteEmail, ConfiguracionRecordatorioRequest request) {
        Estudiante e = estudiante(estudianteEmail);

        LocalTime hora = LocalTime.parse(request.getHora());
        // El envío corre cada 15 minutos, así que la hora se elige en esos intervalos
        if (hora.getMinute() % 15 != 0) {
            throw new BusinessException("Elige una hora en intervalos de 15 minutos (:00, :15, :30 o :45).");
        }

        String dias = null;
        if (request.getFrecuencia() == FrecuenciaRecordatorio.DIAS_ESPECIFICOS) {
            if (request.getDias() == null || request.getDias().isEmpty()) {
                throw new BusinessException("Elige al menos un día de la semana.");
            }
            // Sin repetidos y en orden de la semana
            dias = request.getDias().stream().distinct().sorted()
                    .map(DayOfWeek::name).collect(Collectors.joining(","));
        }

        e.setReminderTime(hora);
        e.setReminderDays(dias);
        estudianteRepository.save(e);
        return toResponse(e);
    }

    // Vuelve a la hora predeterminada del sistema, todos los días
    @Transactional
    public ConfiguracionRecordatorioResponse restablecer(String estudianteEmail) {
        Estudiante e = estudiante(estudianteEmail);
        e.setReminderTime(HORA_PREDETERMINADA);
        e.setReminderDays(null);
        estudianteRepository.save(e);
        return toResponse(e);
    }

    // ¿Le toca recordatorio en este día? Sin días configurados = todos los días
    public static boolean tocaHoy(String reminderDays, LocalDate hoy) {
        if (reminderDays == null || reminderDays.isBlank()) {
            return true;
        }
        return Arrays.asList(reminderDays.split(",")).contains(hoy.getDayOfWeek().name());
    }

    // ───────────── Internos ─────────────

    private Estudiante estudiante(String email) {
        return estudianteRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("Solo un estudiante puede configurar sus recordatorios."));
    }

    private ConfiguracionRecordatorioResponse toResponse(Estudiante e) {
        LocalTime hora = e.getReminderTime() != null ? e.getReminderTime() : HORA_PREDETERMINADA;
        boolean diaria = e.getReminderDays() == null || e.getReminderDays().isBlank();
        List<DayOfWeek> dias = diaria ? List.of()
                : Arrays.stream(e.getReminderDays().split(",")).map(DayOfWeek::valueOf).sorted().toList();
        return ConfiguracionRecordatorioResponse.builder()
                .hora(hora.toString())
                .frecuencia(diaria ? FrecuenciaRecordatorio.DIARIA : FrecuenciaRecordatorio.DIAS_ESPECIFICOS)
                .dias(dias)
                .predeterminada(diaria && hora.equals(HORA_PREDETERMINADA))
                .horaPredeterminada(HORA_PREDETERMINADA.toString())
                .build();
    }
}