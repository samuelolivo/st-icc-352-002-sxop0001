package org.example.models;

public class Usuario {
    private static int count;
    private int id;
    private String usuario;
    private String password;
    private RolesUsuario rol;
    private EstadoObjeto estado;

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        Usuario.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RolesUsuario getRol() {
        return rol;
    }

    public void setRol(RolesUsuario rol) {
        this.rol = rol;
    }

    public EstadoObjeto getEstado() {
        return estado;
    }

    public void setEstado(EstadoObjeto estado) {
        this.estado = estado;
    }
}
