package org.example.services;

import org.example.models.EstadoObjeto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;

import java.util.ArrayList;

public class ServicioUsuario {

    private ArrayList<Usuario> listaUsuarios;

    public ServicioUsuario() {
        listaUsuarios = new ArrayList<>();
        crear("admin", "admin", RolesUsuario.ADMIN);
    }

    public Usuario crear(String username, String password, RolesUsuario rol) {
        Usuario usuario = new Usuario(username, password, rol);
        listaUsuarios.add(usuario);
        return usuario;
    }

    public Usuario buscarPorId(int id) {

        for (Usuario usuario : listaUsuarios) {
            if (usuario.getId() == id) {
                return usuario;
            }
        }

        return null;
    }

    public Usuario buscarPorUsername(String username) {

        for (Usuario usuario : listaUsuarios) {
            if (usuario.getUsuario().equals(username)) {
                return usuario;
            }
        }

        return null;

    }


    public Usuario validarLogin(String username, String password) {

        Usuario usuario = buscarPorUsername(username);

        if (usuario != null &&
                usuario.getPassword().equals(password) &&
                usuario.getEstado() == EstadoObjeto.ACTIVO) {

            return usuario;
        }

        return null;

    }

    public boolean modificarPorId(int id, String nuevoUsername, String nuevoPassword, RolesUsuario nuevoRol) {

        Usuario usuario = buscarPorId(id);

        if (usuario != null && usuario.getEstado() == EstadoObjeto.ACTIVO) {
            usuario.setUsuario(nuevoUsername);
            usuario.setPassword(nuevoPassword);
            usuario.setRol(nuevoRol);

            return true;
        }

        return false;
    }

    public boolean borrarPorId(int id) {

        Usuario usuario = buscarPorId(id);

        if (usuario != null && usuario.getEstado() == EstadoObjeto.ACTIVO) {
            usuario.setEstado(EstadoObjeto.INACTIVO);

            return true;
        }

        return false;
    }

    public ArrayList<Usuario> listarTodos() {
        return new ArrayList<>(listaUsuarios);
    }

    public ArrayList<Usuario> listarActivos() {
        ArrayList<Usuario> activos = new ArrayList<>();

        for (Usuario usuario : listaUsuarios) {
            if (usuario.getEstado() == EstadoObjeto.ACTIVO) {
                activos.add(usuario);
            }
        }

        return activos;
    }
}