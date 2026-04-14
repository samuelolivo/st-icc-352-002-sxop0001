package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.EncuestaController;
import org.example.controllers.RestApiController;
import org.example.controllers.SesionController;
import org.example.controllers.UsuarioController;
import org.example.controllers.WebSocketController;
import org.example.models.RolUsuario;
import org.example.models.Usuario;
import org.example.repository.EncuestaRepository;
import org.example.repository.UsuarioRepository;
import org.example.services.EncuestaService;
import org.example.services.UsuarioService;
import org.jasypt.util.password.BasicPasswordEncryptor;

import java.util.Map;


public class Main {
    public static enum KeySession {
        JWT,
        REFERER;
    }

    public static void main(String[] args) {
        UsuarioRepository usuarioRepository = new UsuarioRepository();
        EncuestaRepository encuestaRepository = new EncuestaRepository();

        UsuarioService usuarioService = new UsuarioService(usuarioRepository, new BasicPasswordEncryptor());
        EncuestaService encuestaService = new EncuestaService(encuestaRepository, usuarioService);

        if (usuarioService.listarTodosActivos().isEmpty()) {
            Usuario adminDefault = new Usuario(
                    "Administrador del Sistema",
                    "admin@admin.com",
                    "admin",
                    RolUsuario.ADMIN
            );
            usuarioService.guardar(adminDefault);
            System.out.println("Base de datos inicializada: Usuario ADMIN creado por defecto.");
        }

        UsuarioController usuarioController = new UsuarioController(usuarioService);
        EncuestaController encuestaController = new EncuestaController(encuestaService);
        SesionController sesionController = new SesionController(usuarioService);
        WebSocketController webSocketController = new WebSocketController();
        RestApiController restApiController = new RestApiController(encuestaService, usuarioService);

        var app = Javalin.create(config -> {
            config.http.maxRequestSize = 10_000_000L;

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/templates";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompressMaxSize = -1;
                staticFiles.headers = Map.of("Service-Worker-Allowed", "/");
            });

            config.fileRenderer(new JavalinThymeleaf());
            config.routes.before("/**", sesionController::sesion);
            config.routes.before("/admin/**", sesionController::adminValido);
            config.routes.before("/encuestador/**", sesionController::encuestadorValido);
            config.routes.get("/", sesionController::defaultPath);
            config.routes.get("/volver", sesionController::volver);

            config.routes.get("/login", sesionController::vistaLogin);
            config.routes.post("/login/procesar", sesionController::procesarLogin);
            config.routes.get("/logout", sesionController::logout);

            config.routes.get("/registro", usuarioController::vistaRegistro);
            config.routes.post("/registrar", usuarioController::registrar);

            config.routes.get("/encuestador/encuesta/crear", encuestaController::mostrarFormulario);
            config.routes.post("/encuestador/encuesta/guardar", encuestaController::crearEncuesta);
            config.routes.get("/encuestador/encuesta/editar/{id}", encuestaController::mostrarEditar);
            config.routes.get("/encuestador/encuesta/eliminar/{id}", encuestaController::eliminar);
            config.routes.get("/encuesta", encuestaController::vistaListar);
            config.routes.get("/encuesta/mapa", encuestaController::vistaMapa);
            config.routes.get("/encuesta/mapa/puntos", encuestaController::listarEncuestasJson);
            config.routes.post("/encuestador/encuesta/sincronizar", encuestaController::sincronizarEncuesta);

            config.routes.get("/usuarios", usuarioController::vistaListar);
            config.routes.get("/admin/usuarios/crear", usuarioController::mostrarFormulario);
            config.routes.post("/admin/usuarios/guardar", usuarioController::guardar);
            config.routes.get("/admin/usuarios/editar/{id}", usuarioController::mostrarEditar);
            config.routes.get("/admin/usuarios/eliminar/{id}", usuarioController::eliminar);

            config.routes.ws("/ws", webSocketController::configurarRutas);

            config.routes.get("/api/rest", restApiController::rest);
            config.routes.post("/api/auth/login", restApiController::login);
            config.routes.before("/api/encuestas", restApiController::validarTokenDelAutenticado);
            config.routes.get("/api/encuestas", restApiController::listarEncuestasPorUsuario);
            config.routes.post("/api/encuestas", restApiController::crearEncuesta);
            config.routes.get("/cliente-grpc", ctx -> ctx.render("templates/clienteGrpc.html"));
            config.routes.post("/api/grpc/procesar", encuestaController::procesarEncuestaGrpc);
        });

        app.start(7070);
    }
}
