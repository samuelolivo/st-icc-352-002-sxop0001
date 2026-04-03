package org.example.models;


public enum NivelEscolar {
    BASICO("Basico"),
    MEDIO("Medio"),
    UNIVERSITARIO("Universitario"),
    POSTGRADO("Postgrado"),
    DOCTORADO("Doctorado");

    private final String descripcion;
    NivelEscolar(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }
}
