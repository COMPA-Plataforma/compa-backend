package com.compa.enums;

// Prioridad de un mensaje de acompañamiento: ALTA invita a hacer algo concreto o a buscar apoyo;
// BAJA es un mensaje de ánimo o un consejo ligero.
public enum PrioridadMensaje {
    ALTA("alta"),
    BAJA("baja");

    private final String displayName;

    PrioridadMensaje(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}