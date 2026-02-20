package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;

import java.util.HashMap;
import java.util.Map;

import static org.example.Main.servicioCarrito;
import static org.example.Main.servicioUsuario;

public class ControladorSesion {
    public static void sesion(Context ctx){
        System.out.println(ctx.path());
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Boolean check = ctx.sessionAttribute("primera_vez");

        if (check == null) {
            ctx.sessionAttribute(Main.KeySession.USUARIO.name(), servicioUsuario.buscarPorUsername(""));
            ctx.sessionAttribute("primera_vez", true);
            ctx.redirect("/productos");
        }
    }

    public static void adminValido(Context ctx){
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (usuario == null || usuario.getRol() != RolesUsuario.ADMIN) {
            String anteriorPath = ctx.header("Referer");
            ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
        }
    }

    public static void defaultPath(Context ctx){
        ctx.redirect("/productos");
    }

    public static void volver(Context ctx){
        String anteriorPath = ctx.sessionAttribute(Main.KeySession.REFERER.name());
        ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
    }

    public static void vistaLogin(Context ctx){
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Map<String, Object> model = new HashMap<>();

        String guardado = ctx.sessionAttribute(Main.KeySession.REFERER.name());
        String actual = ctx.header("Referer");

        if (guardado == null || guardado.isEmpty()) {
            if (actual != null && !actual.contains("/login") && !actual.contains("/procesarLogin")) {
                ctx.sessionAttribute(Main.KeySession.REFERER.name(), actual);
            }
        }

        if (ctx.queryParam("error") != null) {
            model.put("error", "Usuario o contraseña incorrectos.");
        }

        ctx.render("templates/login.html", model);
    }

    public static void procesarLogin(Context ctx){
        String nombre = ctx.formParam("usuario");
        String password = ctx.formParam("password");

        Usuario usuario = servicioUsuario.validarLogin(nombre, password);

        if (usuario != null) {
            ctx.sessionAttribute(Main.KeySession.USUARIO.name(), usuario);

            String anteriorPath = ctx.sessionAttribute(Main.KeySession.REFERER.name());
            ctx.redirect((anteriorPath != null) ? anteriorPath : "/productos");

            servicioCarrito.mergeCarritoLogin(usuario);
        } else {
            ctx.redirect("/login?error=1");
        }
    }

    public static void logout(Context ctx){
        ctx.sessionAttribute(Main.KeySession.USUARIO.name(), servicioUsuario.buscarPorUsername(""));

        String anteriorPath = ctx.header("Referer");
        ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
    }
}
