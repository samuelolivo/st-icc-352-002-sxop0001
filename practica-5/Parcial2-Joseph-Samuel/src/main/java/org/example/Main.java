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
                staticFiles.precompressMaxSize = -1;
            });

            config.routes.before("/**", ControladorSesion::sesion);
            config.routes.before("/admin/**", ControladorSesion::adminValido);
            config.routes.before("/organizador/**", ControladorSesion::organizadorValido);

            config.routes.get("/", ControladorSesion::defaultPath);
            config.routes.get("/volver", ControladorSesion::volver);


            config.routes.get("/login", ControladorSesion::vistaLogin);
            config.routes.post("/procesarLogin", ControladorSesion::procesarLogin);
            config.routes.get("/logout", ControladorSesion::logout);


            config.routes.get("/usuario/lista", ControladorUsuario::vistaListar);
            config.routes.get("/admin/usuario/crear", ControladorUsuario::vistaCrear);
            config.routes.post("/admin/usuario/guardar", ControladorUsuario::crear);
            config.routes.get("/admin/usuario/editar/{id}", ControladorUsuario::vistaModificar);
            config.routes.post("/admin/usuario/actualizar", ControladorUsuario::modificar);
            config.routes.get("/admin/usuario/eliminar/{id}", ControladorUsuario::eliminar);


            config.routes.get("/evento/lista", ControladorEvento::listar);
            config.routes.get("/organizador/evento/crear", ControladorEvento::vistaCrear);
            config.routes.post("/organizador/evento/guardar", ControladorEvento::guardar);
            config.routes.get("/organizador/evento/editar/{id}", ControladorEvento::vistaModificar);
            config.routes.post("/organizador/evento/actualizar", ControladorEvento::modificar);
            config.routes.get("/organizador/evento/eliminar/{id}", ControladorEvento::eliminar);
            config.routes.post("/organizador/evento/publicar/{id}", ControladorEvento::alternarPublicacion);
            config.routes.get("/evento/ver/{id}", ControladorEvento::vistaVer);


            config.routes.post("/inscripcion/inscribirse/{id}", ControladorInscripcion::inscribirse);
            config.routes.get("/inscripcion/mis-inscripciones",       ControladorInscripcion::misInscripciones);
            config.routes.post("/inscripcion/cancelar/{id}",          ControladorInscripcion::cancelar);
            config.routes.get("/inscripcion/qr/{id}",                 ControladorInscripcion::verQr);
            config.routes.get("/organizador/inscripcion/escanear/{id}", ControladorInscripcion::vistaEscanear);
            config.routes.post("/organizador/inscripcion/validar",    ControladorInscripcion::validarQr);

            config.fileRenderer(new JavalinThymeleaf());
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo servidor y cerrando base de datos...");
            BootStrapServices.stopDb();
        }));


        app.start(7070);
    }
}

