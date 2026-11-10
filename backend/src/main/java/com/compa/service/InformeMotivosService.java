package com.compa.service;

import com.compa.dto.response.InformeMotivosResponse;
import com.compa.dto.response.InformeMotivosResponse.MotivoItem;
import com.compa.dto.response.InformeMotivosResponse.Sugerencia;
import com.compa.enums.MotivoConsulta;
import com.compa.exception.BusinessException;
import com.compa.model.AdherenceSnapshot;
import com.compa.model.Atencion;
import com.compa.repository.AdherenceSnapshotRepository;
import com.compa.repository.AtencionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InformeMotivosService {

    private static final ZoneId ZONA = ZoneId.of("America/Bogota");

    // Regla de anonimato: un motivo con menos estudiantes que esto no se muestra
    static final int MINIMO_ESTUDIANTES = 5;

    // Por debajo de este promedio se agrega una nota a la sugerencia
    private static final double ADHERENCIA_BAJA = 50.0;

    private final AtencionRepository atencionRepository;
    private final AdherenceSnapshotRepository snapshotRepository;

    @Transactional(readOnly = true)
    public InformeMotivosResponse generar(LocalDate desde, LocalDate hasta, MotivoConsulta motivoFiltro) {
        LocalDate hoy = LocalDate.now(ZONA);
        LocalDate hastaEfectivo = hasta != null ? hasta : hoy;
        if (desde != null && desde.isAfter(hastaEfectivo)) {
            throw new BusinessException("La fecha inicial no puede ser posterior a la fecha final");
        }

        LocalDateTime inicio = desde != null ? desde.atStartOfDay() : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime fin = hastaEfectivo.plusDays(1).atStartOfDay();

        // Cada estudiante tiene una sola atencion, asi que cada id aparece una vez por motivo
        Map<MotivoConsulta, List<Long>> estudiantesPorMotivo = new EnumMap<>(MotivoConsulta.class);
        for (Atencion a : atencionRepository.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(inicio, fin)) {
            Long estudianteId = a.getEstudiante().getId();
            for (MotivoConsulta m : a.getMotivos()) {
                if (motivoFiltro == null || motivoFiltro == m) {
                    estudiantesPorMotivo.computeIfAbsent(m, k -> new ArrayList<>()).add(estudianteId);
                }
            }
        }

        // Adherencia (cumplimiento mensual) de la foto mas reciente de cada estudiante hasta la fecha final
        Map<Long, Double> adherencia = new HashMap<>();
        List<MotivoItem> visibles = new ArrayList<>();
        boolean hayOmitidos = false;

        for (Map.Entry<MotivoConsulta, List<Long>> e : estudiantesPorMotivo.entrySet()) {
            List<Long> ids = e.getValue();
            if (ids.size() < MINIMO_ESTUDIANTES) {
                hayOmitidos = true;
                continue;
            }
            double suma = 0;
            int conDato = 0;
            for (Long id : ids) {
                if (!adherencia.containsKey(id)) {
                    adherencia.put(id, snapshotRepository
                            .findFirstByEstudiante_IdAndSnapshotDateLessThanEqualOrderBySnapshotDateDesc(id, hastaEfectivo)
                            .map(AdherenceSnapshot::getMonthlyCompliance)
                            .orElse(null));
                }
                Double valor = adherencia.get(id);
                if (valor != null) {
                    suma += valor;
                    conDato++;
                }
            }
            Double promedio = conDato > 0 ? Math.round((suma / conDato) * 10.0) / 10.0 : null;
            visibles.add(MotivoItem.builder()
                    .motivo(e.getKey().name())
                    .motivoNombre(e.getKey().getDisplayName())
                    .estudiantes(ids.size())
                    .adherenciaPromedio(promedio)
                    .build());
        }

        visibles.sort(Comparator.comparingLong(MotivoItem::getEstudiantes).reversed()
                .thenComparing(MotivoItem::getMotivoNombre));

        return InformeMotivosResponse.builder()
                .desde(desde)
                .hasta(hastaEfectivo)
                .motivoFiltro(motivoFiltro != null ? motivoFiltro.name() : null)
                .motivos(visibles)
                .hayMotivosOmitidos(hayOmitidos)
                .sugerencia(sugerir(visibles))
                .build();
    }

    // Sugerencia para el motivo principal entre los visibles. "Otros motivos" solo se usa
    // si es el unico, porque no orienta ningun taller concreto.
    private Sugerencia sugerir(List<MotivoItem> visibles) {
        if (visibles.isEmpty()) {
            return null;
        }
        MotivoItem principal = visibles.stream()
                .filter(m -> !MotivoConsulta.OTROS_MOTIVOS.name().equals(m.getMotivo()))
                .findFirst()
                .orElse(visibles.get(0));

        Sugerencia base = CatalogoTalleres.para(MotivoConsulta.valueOf(principal.getMotivo()));
        if (base == null) {
            return null;
        }

        String nota = base.getNota();
        Double promedio = principal.getAdherenciaPromedio();
        if (promedio != null && promedio < ADHERENCIA_BAJA) {
            String aviso = "La adherencia promedio de este grupo es baja (" + promedio
                    + " %). Conviene reforzar el seguimiento junto con el taller.";
            nota = nota == null ? aviso : nota + " " + aviso;
        }

        return Sugerencia.builder()
                .motivo(base.getMotivo())
                .motivoNombre(base.getMotivoNombre())
                .enfoque(base.getEnfoque())
                .talleres(base.getTalleres())
                .nota(nota)
                .build();
    }
}
