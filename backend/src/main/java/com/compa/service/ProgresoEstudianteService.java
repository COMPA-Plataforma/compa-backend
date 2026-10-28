package com.compa.service;

import com.compa.dto.response.ProgresoPlanResponse;
import com.compa.enums.EstadoValidacion;
import com.compa.enums.PlanStatus;
import com.compa.exception.BusinessException;
import com.compa.exception.ResourceNotFoundException;
import com.compa.model.HabitPlan;
import com.compa.model.HabitTask;
import com.compa.model.TaskCheckIn;
import com.compa.repository.HabitPlanRepository;
import com.compa.repository.TaskCheckInRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProgresoEstudianteService {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");

    // Un rango de fechas con el número de actividades programadas en él
    private record Cubeta(LocalDate from, LocalDate to, int slots) {}

    private final HabitPlanRepository habitPlanRepository;
    private final TaskCheckInRepository taskCheckInRepository;

    public ProgresoEstudianteService(HabitPlanRepository habitPlanRepository,
                                     TaskCheckInRepository taskCheckInRepository) {
        this.habitPlanRepository = habitPlanRepository;
        this.taskCheckInRepository = taskCheckInRepository;
    }

    @Transactional(readOnly = true)
    public ProgresoPlanResponse getProgreso(Long estudianteId, LocalDate from, LocalDate to) {
        HabitPlan plan = habitPlanRepository
                .findByEstudianteIdAndStatus(estudianteId, PlanStatus.ACTIVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Aún no tienes un plan de acompañamiento activo."));

        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la final.");
        }

        LocalDate today = LocalDate.now(BOGOTA);
        LocalDate periodTo = to != null ? to : today;
        LocalDate periodFrom = from != null ? from : plan.getStartDate();
        if (periodFrom.isAfter(periodTo)) {
            periodFrom = periodTo;
        }

        // Reportes del estudiante en el periodo, agrupados por actividad
        List<TaskCheckIn> reports = taskCheckInRepository
                .findByCheckIn_Estudiante_IdAndCheckIn_CheckInDateBetween(estudianteId, periodFrom, periodTo);
        Map<Long, List<TaskCheckIn>> reportsByTask = reports.stream()
                .collect(Collectors.groupingBy(r -> r.getTask().getId()));

        int totalScheduled = 0;
        int totalCompleted = 0;
        int totalNotCompleted = 0;
        int totalValidated = 0;
        int totalNotPerformed = 0;
        int totalToValidate = 0;
        boolean hasReports = false;
        List<ProgresoPlanResponse.ActividadProgreso> activities = new ArrayList<>();

        for (HabitTask task : plan.getTasks()) {
            List<TaskCheckIn> taskReports = reportsByTask.getOrDefault(task.getId(), List.of());
            if (!taskReports.isEmpty()) {
                hasReports = true;
            }

            // Desde cuándo y hasta cuándo cuenta esta actividad (nunca días futuros)
            LocalDate taskStart = Collections.max(Arrays.asList(periodFrom, plan.getStartDate(), agreedDate(task, plan)));
            LocalDate taskEnd = today.isBefore(periodTo) ? today : periodTo;
            if (plan.getEndDate() != null && plan.getEndDate().isBefore(taskEnd)) {
                taskEnd = plan.getEndDate();
            }

            int scheduled = 0;
            int completed = 0;
            int notCompleted = 0;

            // Lo que el psicoorientador validó sobre los reportes de esta actividad
            int validated = 0;
            int notPerformed = 0;
            int toValidate = 0;
            for (TaskCheckIn r : taskReports) {
                if (r.getEstadoValidacion() == EstadoValidacion.VALIDADA) validated++;
                else if (r.getEstadoValidacion() == EstadoValidacion.NO_REALIZADA) notPerformed++;
                else if (r.isCompleted()) toValidate++;
            }

            for (Cubeta cubeta : buildCubetas(task, periodFrom, periodTo, taskStart, taskEnd)) {
                long done = taskReports.stream()
                        .filter(r -> r.isCompleted() && inRange(r.getCheckIn().getCheckInDate(), cubeta))
                        .count();
                long notDone = taskReports.stream()
                        .filter(r -> !r.isCompleted() && inRange(r.getCheckIn().getCheckInDate(), cubeta))
                        .count();

                int cubetaCompleted = (int) Math.min(done, cubeta.slots());
                int cubetaNotCompleted = (int) Math.min(notDone, cubeta.slots() - cubetaCompleted);

                scheduled += cubeta.slots();
                completed += cubetaCompleted;
                notCompleted += cubetaNotCompleted;
            }

            totalScheduled += scheduled;
            totalCompleted += completed;
            totalNotCompleted += notCompleted;
            totalValidated += validated;
            totalNotPerformed += notPerformed;
            totalToValidate += toValidate;

            activities.add(ProgresoPlanResponse.ActividadProgreso.builder()
                    .taskId(task.getId())
                    .name(task.getName())
                    .frequency(describeFrequency(task))
                    .scheduled(scheduled)
                    .completed(completed)
                    .notCompleted(notCompleted)
                    .pending(scheduled - completed - notCompleted)
                    .validated(validated)
                    .notPerformed(notPerformed)
                    .toValidate(toValidate)
                    .build());
        }

        Double percentage = null;
        if (totalScheduled > 0) {
            percentage = Math.round(totalCompleted * 1000.0 / totalScheduled) / 10.0;
        }

        return ProgresoPlanResponse.builder()
                .planName(plan.getName())
                .from(periodFrom)
                .to(periodTo)
                .scheduled(totalScheduled)
                .completed(totalCompleted)
                .notCompleted(totalNotCompleted)
                .pending(totalScheduled - totalCompleted - totalNotCompleted)
                .percentage(percentage)
                .validated(totalValidated)
                .notPerformed(totalNotPerformed)
                .toValidate(totalToValidate)
                .hasReports(hasReports)
                .activities(activities)
                .build();
    }

    // Arma las cubetas (rango + cupos programados) según la frecuencia de la actividad
    private List<Cubeta> buildCubetas(HabitTask task, LocalDate periodFrom, LocalDate periodTo,
                                      LocalDate start, LocalDate end) {
        List<Cubeta> cubetas = new ArrayList<>();

        // Con fecha límite: 1 programada si esa fecha cae dentro del periodo
        if (task.getDueDate() != null) {
            LocalDate due = task.getDueDate();
            if (!due.isBefore(periodFrom) && !due.isAfter(periodTo)) {
                cubetas.add(new Cubeta(periodFrom, periodTo, 1));
            }
            return cubetas;
        }

        if (start.isAfter(end)) {
            return cubetas;
        }

        Set<DayOfWeek> days = parseDays(task);

        if (!days.isEmpty()) {
            // Días específicos: una programada por cada día marcado dentro del rango
            int slots = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (days.contains(d.getDayOfWeek())) {
                    slots++;
                }
            }
            if (slots > 0) {
                cubetas.add(new Cubeta(start, end, slots));
            }
        } else if (task.getWeeklyGoal() != null) {
            // Meta semanal: una cubeta por semana (de lunes a domingo)
            LocalDate segStart = start;
            while (!segStart.isAfter(end)) {
                LocalDate weekEnd = segStart.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                LocalDate segEnd = weekEnd.isAfter(end) ? end : weekEnd;
                int daysInSegment = (int) ChronoUnit.DAYS.between(segStart, segEnd) + 1;
                cubetas.add(new Cubeta(segStart, segEnd, Math.min(task.getWeeklyGoal(), daysInSegment)));
                segStart = segEnd.plusDays(1);
            }
        } else {
            // Sin frecuencia ni fecha: una programada por día
            int daysInRange = (int) ChronoUnit.DAYS.between(start, end) + 1;
            cubetas.add(new Cubeta(start, end, daysInRange));
        }

        return cubetas;
    }

    private boolean inRange(LocalDate date, Cubeta cubeta) {
        return !date.isBefore(cubeta.from()) && !date.isAfter(cubeta.to());
    }

    // Fecha desde la que cuenta la actividad: la de la sesión en que se acordó,
    // o la de creación si es una actividad antigua
    private LocalDate agreedDate(HabitTask task, HabitPlan plan) {
        if (task.getAgreedDate() != null) {
            return task.getAgreedDate();
        }
        if (task.getCreatedAt() != null) {
            return task.getCreatedAt().toLocalDate();
        }
        return plan.getStartDate();
    }

    // Convierte los nombres guardados ("Lunes", "Miercoles"...) en días de la semana
    private Set<DayOfWeek> parseDays(HabitTask task) {
        Set<DayOfWeek> days = new HashSet<>();
        if (task.getSpecificDays() == null) {
            return days;
        }
        for (String name : task.getSpecificDays()) {
            String n = Normalizer.normalize(name, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .trim()
                    .toLowerCase(Locale.ROOT);
            switch (n) {
                case "lunes" -> days.add(DayOfWeek.MONDAY);
                case "martes" -> days.add(DayOfWeek.TUESDAY);
                case "miercoles" -> days.add(DayOfWeek.WEDNESDAY);
                case "jueves" -> days.add(DayOfWeek.THURSDAY);
                case "viernes" -> days.add(DayOfWeek.FRIDAY);
                case "sabado" -> days.add(DayOfWeek.SATURDAY);
                case "domingo" -> days.add(DayOfWeek.SUNDAY);
                default -> { }
            }
        }
        return days;
    }

    // Texto legible de la frecuencia para mostrar en pantalla
    private String describeFrequency(HabitTask task) {
        if (task.getDueDate() != null) {
            return "Fecha límite: " + task.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        if (task.getSpecificDays() != null && !task.getSpecificDays().isEmpty()) {
            return String.join(", ", task.getSpecificDays());
        }
        if (task.getWeeklyGoal() != null) {
            return task.getWeeklyGoal() + (task.getWeeklyGoal() == 1 ? " vez por semana" : " veces por semana");
        }
        return "Todos los días";
    }
}