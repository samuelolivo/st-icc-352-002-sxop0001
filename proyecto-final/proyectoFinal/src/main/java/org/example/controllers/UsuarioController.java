package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.Usuario;
import org.example.services.UsuarioService;
import java.util.HashMap;
import java.util.Map;

public class UsuarioController {
    private static UsuarioService serv = new UsuarioService();

    public static void vistaListar(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        model.put("usuarios", serv.listarTodosActivos());
        ctx.render("templates/usuarios.html", model);
    }

    public static void mostrarFormulario(Context ctx) {
        ctx.render("templates/formularioUsuario.html");
    }

    public static void mostrarEditar(Context ctx) {
        String id = ctx.pathParam("id");
        Usuario u = serv.buscarActivoPorId(id);
        if (u == null) { ctx.redirect("/usuarios"); return; }

        Map<String, Object> model = new HashMap<>();
        model.put("usuario", u);
        model.put("editando", true);
        ctx.render("templates/formularioUsuario.html", model);
    }

    public static void guardar(Context ctx) {
        Usuario u = ctx.bodyAsClass(Usuario.class);
        serv.guardar(u);
        ctx.status(201);
    }

    public static void eliminar(Context ctx) {
        serv.desactivarUsuario(ctx.pathParam("id"));
        ctx.redirect("/usuarios");
    }
}