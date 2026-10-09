package com.compa.enums;

public enum MotivoConsulta {
    ESTRES_ACADEMICO("Estrés académico"),
    ANSIEDAD("Ansiedad"),
    ESTADO_DE_ANIMO("Estado de ánimo"),
    MANEJO_DEL_TIEMPO("Manejo del tiempo"),
    PROCRASTINACION("Procrastinación"),
    SUENO("Sueño"),
    MOTIVACION("Motivación"),
    ADAPTACION_UNIVERSITARIA("Adaptación a la vida universitaria"),
    RELACIONES_INTERPERSONALES("Relaciones interpersonales"),
    ORIENTACION_VOCACIONAL("Orientación vocacional");

    private final String displayName;

    MotivoConsulta(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
