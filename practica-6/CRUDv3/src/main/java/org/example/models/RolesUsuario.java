package org.example.models;

import java.util.Arrays;
import java.util.List;

public enum RolesUsuario {
    ADMIN("Administrador"),
    CLIENTE("Cliente"),
    NO_AUTENTICADO("No Autenticado");

    private final String descripcion;
    RolesUsuario(String descripcion) {
        this.descripcion = descripcion;
    }
    public String getDescripcion() {
        return descripcion;
    }

    public static List<RolesUsuario> rolesSeleccionables() {
        return Arrays.asList(ADMIN, CLIENTE);
    }
}
