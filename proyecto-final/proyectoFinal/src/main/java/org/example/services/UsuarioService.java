package org.example.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.example.models.Usuario;
import org.example.repository.UsuarioRepository;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.Date;
import java.util.List;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BasicPasswordEncryptor passwordEncryptor;

    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
        this.passwordEncryptor = new BasicPasswordEncryptor();
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
        }

        if (usuario.getId() == null) {
            String hash = passwordEncryptor.encryptPassword(usuario.getPassword());
            usuario.setPassword(hash);
        } else {
            Usuario persistido = usuarioRepository.buscarPorId(usuario.getId());
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                usuario.setPassword(persistido.getPassword());
            } else {
                String hash = passwordEncryptor.encryptPassword(usuario.getPassword());
                usuario.setPassword(hash);
            }
        }

        usuario.setEstadoObjeto(true);
        usuarioRepository.guardar(usuario);
    }

    public void modificar(Usuario usuario) {
        buscarActivoPorId(usuario.getId());
        Usuario usuarioAnt = usuarioRepository.buscarPorEmail(usuario.getEmail());

        if (usuarioAnt != null && !usuarioAnt.getId().equals(usuario.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario asociado a este correo.");
        }

        String hash = passwordEncryptor.encryptPassword(usuario.getPassword());
        usuario.setPassword(hash);

        usuarioRepository.guardar(usuario);
    }

    public void desactivarUsuario(String id) {
        buscarActivoPorId(id);
        usuarioRepository.desactivar(id);
    }

    public String autenticar(String email, String passwordPlano) {
        Usuario usuario = usuarioRepository.buscarPorEmail(email);

        if (usuario != null && usuario.isEstadoObjeto()) {
            boolean credencialesValidas = passwordEncryptor.checkPassword(passwordPlano, usuario.getPassword());

            if (credencialesValidas) {
                return generarTokenJWT(usuario);
            }
        }
        return null;
    }

    private String generarTokenJWT(Usuario usuario) {
        try {
            String secretKey = "mi_clave_secreta_para_el_proyecto_icc362";
            Algorithm algorithm = Algorithm.HMAC256(secretKey);

            long tiempoExpiracionEnMilisegundos = 24L * 60 * 60 * 1000;
            Date fechaExpiracion = new Date(System.currentTimeMillis() + tiempoExpiracionEnMilisegundos);

            return JWT.create()
                    .withIssuer("SistemaEncuestasApp")
                    .withSubject(usuario.getEmail())
                    .withClaim("usuarioId", usuario.getId())
                    .withClaim("rol", usuario.getRol().name())
                    .withExpiresAt(fechaExpiracion)
                    .sign(algorithm);

        } catch (Exception e) {
            throw new RuntimeException("Error crítico interno al generar el token de seguridad", e);
        }
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