package org.example.models;


public enum RolUsuario {
    ADMIN("Administrador"),
    ENCUESTADOR("Encuestador"),
    BRECHADOR("Brechador");

    private final String descripcion;
    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
}
