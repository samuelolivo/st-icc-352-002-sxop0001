package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Usuario;
import org.example.services.ServicioComentario;

import static org.example.Main.servicioComentario;

public class ControladorComentario {

    public static void crear(Context ctx) {
        int productoId = Integer.parseInt(ctx.formParam("productoId"));
        String contenido = ctx.formParam("contenido");
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (usuario != null && contenido != null && !contenido.isBlank()) {
            servicioComentario.crear(contenido, usuario.getUsuario(), productoId);
        }
        ctx.redirect("/producto/ver/" + productoId);
    }

    public static void eliminar(Context ctx) {
        Long id = Long.parseLong(ctx.pathParam("idComentario"));
        int productoId = Integer.parseInt(ctx.pathParam("idProducto"));
        servicioComentario.eliminar(id);
        ctx.redirect("/producto/ver/" + productoId);
    }
}