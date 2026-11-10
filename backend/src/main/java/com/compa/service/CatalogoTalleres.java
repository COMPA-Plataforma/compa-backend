package com.compa.service;

import com.compa.dto.response.InformeMotivosResponse.Sugerencia;
import com.compa.enums.MotivoConsulta;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// Catalogo orientativo de talleres por motivo de consulta. Es una sugerencia por reglas,
// no un diagnostico: la decision final es de Bienestar.
public final class CatalogoTalleres {

    private static final String NOTA_SENSIBLE =
            "Este tema se aborda con enfoque informativo y de rutas de atención. "
                    + "La atención individual sigue siendo del psicoorientador.";

    private static final Map<MotivoConsulta, Sugerencia> CATALOGO = new EnumMap<>(MotivoConsulta.class);

    static {
        agregar(MotivoConsulta.BIENESTAR_EMOCIONAL,
                "Manejo del estrés y la ansiedad",
                List.of("Taller de manejo de la ansiedad ante la carga académica",
                        "Taller de técnicas de relajación y regulación emocional"),
                null);
        agregar(MotivoConsulta.DIFICULTADES_ACADEMICAS,
                "Organización del estudio y la carga académica",
                List.of("Taller de organización del tiempo y hábitos de estudio",
                        "Taller de técnicas de estudio y preparación de evaluaciones"),
                null);
        agregar(MotivoConsulta.RELACIONES_INTERPERSONALES,
                "Habilidades para la convivencia",
                List.of("Taller de comunicación asertiva",
                        "Taller de manejo de conflictos y trabajo en equipo"),
                null);
        agregar(MotivoConsulta.SITUACIONES_FAMILIARES,
                "Equilibrio entre la vida familiar y la universitaria",
                List.of("Taller de equilibrio entre vida familiar y universitaria",
                        "Espacio de apoyo para afrontar situaciones familiares difíciles"),
                null);
        agregar(MotivoConsulta.ADAPTACION_PERMANENCIA,
                "Adaptación y permanencia en la universidad",
                List.of("Taller de adaptación a la vida universitaria",
                        "Taller de estrategias de permanencia y uso de los servicios de Bienestar"),
                null);
        agregar(MotivoConsulta.PROYECTO_VIDA_VOCACIONAL,
                "Proyecto de vida y decisiones vocacionales",
                List.of("Taller de proyecto de vida",
                        "Taller de orientación vocacional y toma de decisiones"),
                null);
        agregar(MotivoConsulta.SALUD_MENTAL_SEGUIMIENTO,
                "Autocuidado y rutas de atención en salud mental",
                List.of("Jornada informativa sobre rutas de atención en salud mental y autocuidado",
                        "Difusión de la Línea ALMA (opción 1, salud mental) y de la atención por EPS"),
                NOTA_SENSIBLE);
        agregar(MotivoConsulta.VIOLENCIAS_GENERO_SEXUALES,
                "Prevención de violencias basadas en género",
                List.of("Taller de prevención de violencias basadas en género y rutas de atención",
                        "Difusión de la Línea ALMA (opción 2, orientación ante violencias)"),
                NOTA_SENSIBLE);
        agregar(MotivoConsulta.OTROS_MOTIVOS,
                "Motivos sin categoría específica",
                List.of("Revisar con el equipo los motivos registrados como «otros» "
                        + "para identificar categorías nuevas"),
                null);
    }

    private CatalogoTalleres() {
    }

    private static void agregar(MotivoConsulta motivo, String enfoque, List<String> talleres, String nota) {
        CATALOGO.put(motivo, Sugerencia.builder()
                .motivo(motivo.name())
                .motivoNombre(motivo.getDisplayName())
                .enfoque(enfoque)
                .talleres(talleres)
                .nota(nota)
                .build());
    }

    public static Sugerencia para(MotivoConsulta motivo) {
        return CATALOGO.get(motivo);
    }
}