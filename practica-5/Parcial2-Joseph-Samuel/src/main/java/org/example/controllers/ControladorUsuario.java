package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;

import java.util.HashMap;
import java.util.Map;

import static org.example.Main.servicioUsuario;

public class ControladorUsuario {

    public static void vistaListar(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Map<String, Object> model = new HashMap<>();


        model.put("usuarios", servicioUsuario.listarActivos());
        model.put("usuario", usuarioLogueado);

        ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/usuario/lista");
        ctx.render("templates/usuarios.html", model);
    }

    public static void bloquear(Context ctx) {
        int id = Integer.parseInt(ctx.formParam("id"));
        servicioUsuario.bloquear(id);
        ctx.redirect("/usuario/lista");
    }

    public static void vistaCrear(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("roles", RolesUsuario.rolesSeleccionables());

        if (ctx.queryParam("error") != null) {
            String error = ctx.queryParam("error");
            if ("1".equals(error)) model.put("error", "El nombre de usuario ya existe.");
            if ("2".equals(error)) model.put("error", "Las contraseñas no coinciden.");
        }

        ctx.render("templates/admin/crearUsuario.html", model);
    }

    public static void crear(Context ctx) {
        String usuario = ctx.formParam("usuario");
        String password = ctx.formParam("password");
        String passwordConfirm = ctx.formParam("passwordConfirm");
        String rolStr = ctx.formParam("rol");

        if (servicioUsuario.buscarPorUsername(usuario) != null) {
            ctx.redirect("/admin/usuario/crear?error=1");
            return;
        }

        if (password == null || !password.equals(passwordConfirm)) {
            ctx.redirect("/admin/usuario/crear?error=2");
            return;
        }

        RolesUsuario rol = RolesUsuario.valueOf(rolStr);
        servicioUsuario.crear(usuario, password, rol);

        ctx.redirect("/usuario/lista");
    }

    public static void vistaModificar(Context ctx) {
        String username = ctx.pathParam("id");
        Usuario usuario = servicioUsuario.buscarPorUsername(username);

        if (usuario != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("usuarioEditar", usuario);
            model.put("roles", RolesUsuario.rolesSeleccionables());
            ctx.render("templates/admin/editarUsuario.html", model);
        } else {
            ctx.redirect("/usuario/lista");
        }
    }

    public static void modificar(Context ctx) {
        int id = Integer.parseInt(ctx.formParam("id"));
        String usuario = ctx.formParam("usuario");
        String password = ctx.formParam("password");
        String rolStr = ctx.formParam("rol");
        RolesUsuario rol = RolesUsuario.valueOf(rolStr);

        Usuario userEdit = servicioUsuario.buscarPorId(id);


        if (userEdit != null && "admin".equals(userEdit.getUsuario())) {
            ctx.redirect("/usuario/lista");
            return;
        }

        servicioUsuario.modificarPorId(id, usuario, password, rol);
        ctx.redirect("/usuario/lista");
    }

    public static void eliminar(Context ctx) {
        String username = ctx.pathParam("id");

        if ("admin".equals(username)) {
            ctx.redirect("/usuario/lista");
            return;
        }

        servicioUsuario.borrarPorUsername(username);
        ctx.redirect("/usuario/lista");
    }
}