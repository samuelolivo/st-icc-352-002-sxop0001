package org.example.models;

public enum EstadoSincronizacion {
    PENDIENTE("Pendiente"),
    SINCRONIZADO("Sincronizado"),
    ERROR("Error");

    private final String descripcion;
    EstadoSincronizacion(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
}
