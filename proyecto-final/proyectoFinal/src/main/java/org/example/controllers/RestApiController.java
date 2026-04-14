package org.example.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import org.example.models.Encuesta;
import org.example.models.EstadoSincronizacion;
import org.example.models.Usuario;
import org.example.services.EncuestaService;
import org.example.services.UsuarioService;
import org.example.utils.JwtUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestApiController {

    private final EncuestaService encuestaService;
    private final UsuarioService usuarioService;

    public RestApiController(EncuestaService encuestaService, UsuarioService usuarioService) {
        this.encuestaService = encuestaService;
        this.usuarioService = usuarioService;
    }

    public void rest(Context ctx) {
        Map<String, Object> modelo = new HashMap<>();
        List<Usuario> usuarios = usuarioService.listarTodosActivos();
        modelo.put("usuarios", usuarios);
        modelo.put("apiBaseUrl", "");
        ctx.render("templates/cliente-rest.html", modelo);
    }

    public void login(Context ctx) {
        Map<String, String> body = ctx.bodyAsClass(Map.class);
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            ctx.status(400).json(Map.of("error", "Email y password son requeridos."));
            return;
        }

        String token = usuarioService.autenticar(email, password);

        if (token == null) {
            ctx.status(401).json(Map.of("error", "Credenciales inválidas."));
            return;
        }

        ctx.status(200).json(Map.of("token", token));
    }

    public void listarEncuestasPorUsuario(Context ctx) {
        String usuarioId = ctx.queryParam("usuarioId");

        if (usuarioId == null || usuarioId.isBlank()) {
            ctx.status(400).json(Map.of("error", "El parámetro 'usuarioId' es requerido."));
            return;
        }

        try {
            List<Encuesta> encuestas = encuestaService.listarTodasActivasPorUsuario(usuarioId);
            ctx.status(200).json(encuestas);
        } catch (IllegalArgumentException e) {
            ctx.status(404).json(Map.of("error", e.getMessage()));
        }
    }

    public void crearEncuesta(Context ctx) {
        Encuesta nuevaEncuesta = ctx.bodyAsClass(Encuesta.class);
        String idAutenticado = ctx.attribute("usuarioId");
        LocalDateTime fechaCreacion = LocalDateTime.now();

        nuevaEncuesta.setFechaCreacion(fechaCreacion);
        nuevaEncuesta.setFechaSincronizacion(fechaCreacion);
        nuevaEncuesta.setEstadoSync(EstadoSincronizacion.SINCRONIZADO);
        nuevaEncuesta.setUsuarioId(idAutenticado);
        encuestaService.guardar(nuevaEncuesta);
        ctx.status(201).json(nuevaEncuesta);
    }

    public void validarTokenDelAutenticado(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                DecodedJWT djwt = JwtUtil.validarToken(token);
                String usuarioIdExtraido = djwt.getSubject();
                ctx.attribute("usuarioId", usuarioIdExtraido);
            } catch (Exception e) {
                ctx.status(401).json(Map.of("error", "Token inválido, manipulado o expirado"));
                ctx.skipRemainingHandlers();
            }
        } else {
            ctx.status(401).json(Map.of("error", "Requiere autenticación Bearer"));
            ctx.skipRemainingHandlers();
        }
    }
}
