package org.example.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import org.example.Main;
import org.example.models.RolUsuario;
import org.example.models.Usuario;
import org.example.services.UsuarioService;
import org.example.utils.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void vistaListar(Context ctx) {
        String jwt = ctx.sessionAttribute(Main.KeySession.JWT.name());
        String nombre = null;
        String rol = null;

        if (jwt != null) {
            DecodedJWT djwt = JWT.decode(jwt);
            nombre = djwt.getClaim("nombre").asString();
            rol = djwt.getClaim("rol").asString();
        }

        Map<String, Object> model = new HashMap<>();
        model.put("usuarios", usuarioService.listarTodosActivos());
        model.put("nombre", nombre);
        model.put("rol", rol);
        ctx.render("templates/usuarios.html", model);
    }

    public void mostrarFormulario(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("templates/formularioUsuario.html");
    }

    public void mostrarEditar(Context ctx) {
        String id = ctx.pathParam("id");
        Usuario u = usuarioService.buscarActivoPorId(id);
        if (u == null) { ctx.redirect("/usuarios"); return; }

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", u);
        model.put("editando", true);
        ctx.render("templates/formularioUsuario.html", model);
    }

    public void guardar(Context ctx) {
        Usuario u = ctx.bodyAsClass(Usuario.class);
        usuarioService.guardar(u);
        ctx.status(201);
    }

    public void eliminar(Context ctx) {
        usuarioService.desactivarUsuario(ctx.pathParam("id"));
        ctx.redirect("/usuarios");
    }

    public void vistaRegistro(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("templates/registro.html", model);
    }

    public void registrar(Context ctx) {
        try {
            String nombre = ctx.formParam("nombre");
            String email = ctx.formParam("email");
            String password = ctx.formParam("password");
            String passwordConfirmacion = ctx.formParam("passwordConfirmacion");


            if (email == null || email.trim().isEmpty()) {
                ctx.redirect("/registro?error=1");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                ctx.redirect("/registro?error=2");
                return;
            }

            if (!password.equals(passwordConfirmacion)) {
                ctx.redirect("/registro?error=3");
                return;
            }

            if (usuarioService.buscarActivoPorEmail(email) != null) {
                ctx.redirect("/registro?error=4");
                return;
            }

            if (nombre == null || nombre.trim().isEmpty()) {
                ctx.redirect("/registro?error=6");
                return;
            }


            Usuario nuevoUsuario = new Usuario(nombre, email, password, RolUsuario.BRECHADOR);
            usuarioService.guardar(nuevoUsuario);


            ctx.redirect("/login?success=1");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.redirect("/registro?error=5");
        }
    }
}