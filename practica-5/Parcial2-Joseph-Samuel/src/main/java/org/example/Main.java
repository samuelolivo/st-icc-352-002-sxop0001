package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.*;
import org.example.services.*;

public class Main {
    public enum KeySession {
        USUARIO,
        USUARIO_NA,
        REFERER
    }

    public static ServicioUsuario servicioUsuario = new ServicioUsuario();

    static void main() {
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

            config.routes.get("/registro", ControladorRegistro::vistaRegistro);
            config.routes.post("/registrar", ControladorRegistro::registrar);


            config.routes.get("/usuario/lista", ControladorUsuario::vistaListar);
            config.routes.post("/admin/usuario/bloquear", ControladorUsuario::bloquear);
            config.routes.get("/admin/usuario/crear", ControladorUsuario::vistaCrear);
            config.routes.post("/admin/usuario/guardar", ControladorUsuario::crear);
            config.routes.get("/admin/usuario/editar/{id}", ControladorUsuario::vistaModificar);
            config.routes.post("/admin/usuario/actualizar", ControladorUsuario::modificar);
            config.routes.get("/admin/usuario/eliminar/{id}", ControladorUsuario::eliminar);


            config.routes.get("/evento/lista", ControladorEvento::listar);
            config.routes.get("organizador/mis-eventos", ControladorEvento::miseventos);
            config.routes.get("/organizador/evento/crear", ControladorEvento::vistaCrear);
            config.routes.post("/organizador/evento/guardar", ControladorEvento::guardar);
            config.routes.get("/organizador/evento/editar/{id}", ControladorEvento::vistaModificar);
            config.routes.post("/organizador/evento/actualizar", ControladorEvento::modificar);
            config.routes.get("/organizador/evento/eliminar/{id}", ControladorEvento::eliminar);
            config.routes.post("/organizador/evento/publicar/{id}", ControladorEvento::alternarPublicacion);
            config.routes.get("/evento/ver/{id}", ControladorEvento::vistaVer);


            config.routes.get("/inscripcion/inscribirse/{id}", ControladorInscripcion::inscribirse);
            config.routes.get("/inscripcion/desinscribirse/{id}", ControladorInscripcion::desinscribirse);
            config.routes.get("/mis-inscripciones", ControladorInscripcion::misinscripciones);

            config.routes.get("/organizador/escanear-qr", ControladorInscripcion::vistaEscanearQR);
            config.routes.post("/api/qr/validar", ControladorInscripcion::validarQR);
            config.routes.get("/api/qr/obtener/{id}", ControladorInscripcion::obtenerQR);
            config.routes.get("/api/inscripcion/obtener/{eventoId}", ControladorInscripcion::obtenerInscripcionUsuario);

            config.fileRenderer(new JavalinThymeleaf());
        });


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo servidor y cerrando base de datos...");
            BootStrapServices.stopDb();
        }));


        app.start(7070);
    }
}

