package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.Encuesta;
import org.example.repository.EncuestaRepository;
import org.example.services.EncuestaService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncuestaController {
    private static EncuestaService serv = new EncuestaService();

    public static void mostrarFormulario(Context ctx) {
        ctx.render("templates/formularioEncuesta.html");
    }


    public static void crearEncuesta(Context ctx) {
        Encuesta nueva = ctx.bodyAsClass(Encuesta.class);
        serv.guardar(nueva);
        ctx.status(201).json(nueva);
    }

    public static void vistaMapa(Context ctx) {
        ctx.render("templates/mapaEncuestas.html");
    }
    public static void vistaListar(Context ctx) {
        List<Encuesta> lista = serv.listarTodasActivas();

        Map<String, Object> model = new HashMap<>();
        model.put("encuestas", lista);

        ctx.render("templates/encuestas.html", model);
    }

    public static void mostrarEditar(Context ctx) {
        String id = ctx.pathParam("id");
        Encuesta encuesta = serv.buscarPorId(id);

        if (encuesta == null) {
            ctx.redirect("/encuesta");
            return;
        }

        Map<String, Object> model = new HashMap<>();
        model.put("encuesta", encuesta);
        model.put("editando", true);

        ctx.render("templates/formularioEncuesta.html", model);


    }

    public static void eliminar(Context ctx) {
        String id = ctx.pathParam("id");
        serv.desactivar(id);
        ctx.redirect("/encuesta");
    }
    public static void listarEncuestasJson(Context ctx) {
        List<Encuesta> lista = serv.listarTodasActivas();
        ctx.json(lista);
    }
}