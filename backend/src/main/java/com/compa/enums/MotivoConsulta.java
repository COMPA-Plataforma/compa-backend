package com.compa.enums;

public enum MotivoConsulta {
    BIENESTAR_EMOCIONAL("Bienestar emocional"),
    DIFICULTADES_ACADEMICAS("Dificultades académicas"),
    RELACIONES_INTERPERSONALES("Relaciones interpersonales"),
    SITUACIONES_FAMILIARES("Situaciones familiares"),
    ADAPTACION_PERMANENCIA("Adaptación y permanencia universitaria"),
    PROYECTO_VIDA_VOCACIONAL("Proyecto de vida y orientación vocacional"),
    SALUD_MENTAL_SEGUIMIENTO("Salud mental y seguimiento"),
    VIOLENCIAS_GENERO_SEXUALES("Violencias basadas en género y violencias sexuales"),
    OTROS_MOTIVOS("Otros motivos de orientación");

    private final String displayName;

    MotivoConsulta(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
