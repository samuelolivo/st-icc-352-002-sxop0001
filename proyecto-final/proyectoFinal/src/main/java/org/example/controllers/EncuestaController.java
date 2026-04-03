package org.example.controllers;

import io.javalin.http.Context;
import org.example.models.Encuesta;
import org.example.repository.EncuestaRepository;
import java.util.List;

public class EncuestaController {
    private static EncuestaRepository repo = new EncuestaRepository();

    public static void mostrarFormulario(Context ctx) {
        ctx.render("templates/formularioEncuesta.html");
    }


    public static void crearEncuesta(Context ctx) {
        Encuesta nueva = ctx.bodyAsClass(Encuesta.class);
        repo.guardar(nueva);
        ctx.status(201).json(nueva);
    }

    public static void vistaMapa(Context ctx) {
        ctx.render("templates/mapaEncuestas.html");
    }

    public static void listarEncuestasJson(Context ctx) {
        List<Encuesta> lista = repo.listarTodo();
        ctx.json(lista);
    }
}