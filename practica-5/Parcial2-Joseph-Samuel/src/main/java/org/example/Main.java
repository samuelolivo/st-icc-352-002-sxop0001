package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.*;
import org.example.services.*;

public class Main {
    public static enum KeySession {
        USUARIO,
        USUARIO_NA,
        REFERER;
    }

    public static ServicioUsuario servicioUsuario = new ServicioUsuario();

    public static void main(String[] args) {


        BootStrapServices.startDb();
        BootStrapServices.init();


        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/templates";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = false;
            });

            config.fileRenderer(new JavalinThymeleaf());
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo servidor y cerrando base de datos...");
            BootStrapServices.stopDb();
        }));


        app.start(7070);


        app.before("/**", ControladorSesion::sesion);
        app.before("/admin/**", ControladorSesion::adminValido);
        app.get("/", ControladorSesion::defaultPath);
        app.get("/volver", ControladorSesion::volver);


        app.get("/login", ControladorSesion::vistaLogin);
        app.post("/procesarLogin", ControladorSesion::procesarLogin);
        app.get("/logout", ControladorSesion::logout);

        app.get("/usuario/lista", ControladorUsuario::vistaListar);
        app.get("/admin/usuario/crear", ControladorUsuario::vistaCrear);
        app.post("/admin/usuario/guardar", ControladorUsuario::crear);
        app.get("/admin/usuario/editar/{id}", ControladorUsuario::vistaModificar);
        app.post("/admin/usuario/actualizar", ControladorUsuario::modificar);
        app.get("/admin/usuario/eliminar/{id}", ControladorUsuario::eliminar);
    }
}

