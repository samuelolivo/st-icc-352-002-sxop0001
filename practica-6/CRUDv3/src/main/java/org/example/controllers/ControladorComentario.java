package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Usuario;

import static org.example.Main.servicioComentario;

public class ControladorComentario {

    public static void crear(Context ctx){
        String productoIdStr = ctx.formParam("productoId");
        String contenido = ctx.formParam("contenido");
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        if (usuario != null && contenido != null && !contenido.isBlank() && productoIdStr != null) {
            int productoId = Integer.parseInt(productoIdStr);
            servicioComentario.crear(contenido, usuario.getUsuario(), productoId);
            ctx.redirect("/producto/ver/" + productoId);
        } else {
            ctx.redirect("/");
        }
    }

    public static void eliminar(Context ctx) {
        Long idComentario = Long.parseLong(ctx.pathParam("idComentario"));
        String idProducto = ctx.pathParam("idProducto");

        servicioComentario.eliminar(idComentario);
        ControladorNotificacion.notificarEliminacionComentario(idComentario);

        ctx.redirect("/producto/ver/" + idProducto);
    }
}