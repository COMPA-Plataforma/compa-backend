package com.compa.enums;

public enum EstadoDerivacion {
    DERIVADO("Derivado"),
    EN_ESPERA("En espera"),
    ACEPTADO("Aceptado"),
    CITA_ASIGNADA("Cita asignada"),
    EN_ATENCION("En atención");

    private final String etiqueta;

    EstadoDerivacion(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}