package com.compa.enums;

public enum DestinoDerivacion {
    EPS("EPS"),
    HOSPITAL_ALMA_MATER("Hospital Alma Mater"),
    OTRA("Otra ruta institucional o externa");

    private final String etiqueta;

    DestinoDerivacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}