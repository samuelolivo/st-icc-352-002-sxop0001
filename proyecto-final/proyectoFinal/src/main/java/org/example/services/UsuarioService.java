package org.example.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.example.models.Usuario;
import org.example.repository.UsuarioRepository;
import org.example.utils.JwtUtil;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.Date;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BasicPasswordEncryptor passwordEncryptor;

    public UsuarioService(UsuarioRepository usuarioRepository, BasicPasswordEncryptor passwordEncryptor) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncryptor = passwordEncryptor;
    }

    public void guardar(Usuario usuario) {

        if (usuario.getId() != null && usuario.getId().trim().isEmpty()) {
            usuario.setId(null);
        }

        Usuario usuarioExistente = usuarioRepository.buscarPorEmail(usuario.getEmail());
        if (usuarioExistente != null) {
            if (usuario.getId() == null || !usuarioExistente.getId().equals(usuario.getId())) {
                throw new IllegalArgumentException("Ya existe un usuario asociado a este correo.");
            }

            if (usuarioExistente.getEmail().equals("admin@admin.com")){
                throw new IllegalArgumentException("Este usuario no puede ser modificado.");
            }
        }

        if (usuario.getId() == null) {
            String hash = passwordEncryptor.encryptPassword(usuario.getPassword());
            usuario.setPassword(hash);
        } else {
            Usuario usuarioEnDB = usuarioRepository.buscarPorId(usuario.getId());
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(usuarioEnDB.getPassword());
            } else {
                String hash = passwordEncryptor.encryptPassword(usuario.getPassword());
                usuario.setPassword(hash);
            }
        }

        usuario.setEstadoObjeto(true);
        usuarioRepository.guardar(usuario);
    }

    public void desactivarUsuario(String id) {
        Usuario u = buscarActivoPorId(id);
        if (!u.getEmail().equals("admin@admin.com")) {
            usuarioRepository.desactivar(id);
        }
    }

    public String autenticar(String email, String passwordPlano) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email);

        if (usuario != null && usuario.isEstadoObjeto()) {
            boolean credencialesValidas = passwordEncryptor.checkPassword(passwordPlano, usuario.getPassword());

            if (credencialesValidas) {
                return JwtUtil.generarToken(usuario);
            }
        }
        return null;
    }

    public Usuario buscarActivoPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id del usuario no puede estar vacío.");
        }

        Usuario usuario = usuarioRepository.buscarPorId(id);

        if (usuario == null || !usuario.isEstadoObjeto()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        return usuario;
    }

    public Usuario buscarActivoPorEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email del usuario no puede estar vacío.");
        }

        Usuario usuario = usuarioRepository.buscarPorEmail(email);

        if (usuario == null || !usuario.isEstadoObjeto()) {
            throw new IllegalArgumentException("El usuario no existe.");
        }

        return usuario;
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.listarTodo();
    }

    public List<Usuario> listarTodosActivos() {
        return usuarioRepository.listarTodo()
                .stream()
                .filter(Usuario::isEstadoObjeto)
                .toList();
    }
}