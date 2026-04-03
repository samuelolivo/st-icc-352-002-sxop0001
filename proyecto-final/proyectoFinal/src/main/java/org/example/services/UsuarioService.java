package org.example.services;

import org.example.models.Usuario;
import org.example.repository.UsuarioRepository;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }

    public void crearUsuario(Usuario usuario) {
        // NOTA: Antes de guardar, aquí deberías usar la librería Jasypt
        // para encriptar la contraseña (hash) por seguridad.
        // String hash = jasypt.encrypt(usuario.getPassword());
        // usuario.setPassword(hash);

        usuarioRepository.guardar(usuario);
    }

    public String autenticar(String email, String password) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email);

        if (usuario != null) {
            // Aquí compararías la contraseña ingresada con el hash guardado usando Jasypt
            boolean credencialesValidas = usuario.getPassword().equals(password);

            if (credencialesValidas) {
                return generarTokenJWT(usuario);
            }
        }
        return null; // Retorna null si las credenciales fallan o si el usuario fue desactivado
    }

    // Método JWT (Requerimiento 17)
    private String generarTokenJWT(Usuario usuario) {
        return "gustavo";
    }

    public Usuario buscarPorId(String id) {
        return usuarioRepository.buscarPorId(id);
    }

    public List<Usuario> listarTodo() {
        return usuarioRepository.listarTodo();
    }

    public void desactivarUsuario(String id) {
        usuarioRepository.desactivar(id);
    }
}