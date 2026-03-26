package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.*;
import org.example.services.*;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;

public class Main {

    public static enum KeySession {
        USUARIO,
        USUARIO_NA,
        CARRITO_NA,
        REFERER;
    }

    public static ServicioUsuario servicioUsuario = ServicioUsuario.getInstancia();
    public static ServicioProducto servicioProducto = ServicioProducto.getInstancia();
    public static ServicioCarrito servicioCarrito = ServicioCarrito.getInstancia();
    public static ServicioVenta servicioVenta = ServicioVenta.getInstancia();
    public static ServicioComentario servicioComentario = ServicioComentario.getInstancia();

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
            config.routes.get("/", ControladorSesion::defaultPath);
            config.routes.get("/volver", ControladorSesion::volver);

            config.routes.get("/login", ControladorSesion::vistaLogin);
            config.routes.post("/login/procesar", ControladorSesion::procesarLogin);
            config.routes.get("/logout", ControladorSesion::logout);

            config.routes.get("/registro", ControladorRegistro::vistaRegistro);
            config.routes.post("/registrar", ControladorRegistro::registrar);

            config.routes.get("/producto/lista", ControladorProducto::vistaListar);
            config.routes.get("/producto/ver/{id}", ControladorProducto::vistaVer);
            config.routes.get("/admin/producto/crear", ControladorProducto::vistaCrear);
            config.routes.post("/admin/producto/guardar", ControladorProducto::crear);
            config.routes.get("/admin/producto/eliminar/{id}", ControladorProducto::eliminar);
            config.routes.get("/admin/producto/editar/{id}", ControladorProducto::vistaModificar);
            config.routes.post("/admin/producto/actualizar", ControladorProducto::modificar);

            config.routes.get("/carrito", ControladorCarrito::vistaListar);
            config.routes.get("/carrito/agregar/{id}", ControladorCarrito::agregar);
            config.routes.get("/carrito/eliminar/{id}", ControladorCarrito::eliminar);
            config.routes.get("/carrito/vaciar", ControladorCarrito::vaciar);
            config.routes.post("/carrito/procesar", ControladorCarrito::procesar);

            config.routes.get("/usuario/lista", ControladorUsuario::vistaListar);
            config.routes.post("/admin/usuario/bloquear", ControladorUsuario::bloquear);
            config.routes.get("/admin/usuario/crear", ControladorUsuario::vistaCrear);
            config.routes.post("/admin/usuario/guardar", ControladorUsuario::crear);
            config.routes.get("/admin/usuario/editar/{id}", ControladorUsuario::vistaModificar);
            config.routes.post("/admin/usuario/actualizar", ControladorUsuario::modificar);
            config.routes.get("/admin/usuario/eliminar/{id}", ControladorUsuario::eliminar);

            config.routes.get("/admin/ventas", ControladorVenta::vistaListar);

            config.routes.post("/comentarios/crear", ControladorComentario::crear);
            config.routes.get("/admin/comentarios/eliminar/{idComentario}/{idProducto}", ControladorComentario::eliminar);
            config.routes.ws("/ws/notificaciones", ControladorNotificacion::gestionarWebsocket);

            config.fileRenderer(new JavalinThymeleaf());
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Deteniendo servidor y cerrando base de datos...");
            BootStrapServices.stopDb();
        }));

        app.start(7070);
    }
}

