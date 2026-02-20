package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Usuario;

import java.util.HashMap;
import java.util.Map;

import static org.example.Main.servicioVenta;

public class ControladorVenta {

    public static void vistaListar(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        Map<String, Object> model = new HashMap<>();

        model.put("ventas", servicioVenta.listarTodas());
        model.put("usuario", usuarioLogueado);

        ctx.render("templates/admin/ventas.html", model);
    }
}
