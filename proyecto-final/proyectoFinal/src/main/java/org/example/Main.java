package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.EncuestaController;


public class Main {

    public static void main(String[] args) {


        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/templates";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompressMaxSize = -1;
            });


            config.fileRenderer(new JavalinThymeleaf());
            config.routes.get("/", ctx -> ctx.redirect("/encuesta"));
            config.routes.get("/encuesta/crear", EncuestaController::mostrarFormulario);
            config.routes.post("/encuesta/guardar", EncuestaController::crearEncuesta);

            config.routes.get("/admin/encuesta/editar/{id}", EncuestaController::mostrarEditar);
            config.routes.get("/admin/encuesta/eliminar/{id}", EncuestaController::eliminar);

            config.routes.get("/encuesta", EncuestaController::vistaListar);
            config.routes.get("/encuesta/mapa", EncuestaController::vistaMapa);

        });



        app.start(7070);
    }
}