package org.example.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolesUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoObjeto estado;

    @Column(nullable = false)
    private boolean bloqueado = false;


    public Usuario() {
        this.estado = EstadoObjeto.ACTIVO;
    }

    public Usuario(String usuario, String password, RolesUsuario rol) {
        this();
        this.usuario = usuario;
        this.password = password;
        this.rol = rol;
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

    public boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public static Usuario usuarioInvitado() {
        Usuario invitado = new Usuario();
        invitado.setId(-1);
        invitado.setUsuario("Invitado");
        invitado.setPassword("");
        invitado.setRol(RolesUsuario.NO_AUTENTICADO);
        invitado.setEstado(EstadoObjeto.ACTIVO);
        return invitado;
    }
}
