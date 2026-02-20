package org.example;

import io.javalin.Javalin;
import io.javalin.http.ContentTooLargeResponse;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.controllers.*;
import org.example.models.Producto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;
import org.example.services.ServicioCarrito;
import org.example.services.ServicioProducto;
import org.example.services.ServicioUsuario;
import org.example.services.ServicioVenta;

import java.math.BigDecimal;
import java.util.*;

import static java.lang.Thread.sleep;

public class Main {

    public static enum KeySession {
        USUARIO,
        REFERER;
    }

    public static ServicioUsuario servicioUsuario = new ServicioUsuario();
    public static ServicioProducto servicioProducto = new ServicioProducto();
    public static ServicioCarrito servicioCarrito = new ServicioCarrito();
    public static ServicioVenta servicioVenta = new ServicioVenta();


    public static void main(String[] args) {
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/templates";
                staticFiles.location = Location.CLASSPATH;
                staticFiles.precompress = false;
                staticFiles.aliasCheck = null;
            });
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7070);


        app.before("/**", ControladorSesion::sesion);
        app.before("/admin/**", ControladorSesion::adminValido);
        app.get("/", ControladorSesion::defaultPath);
        app.get("/volver", ControladorSesion::volver);



        app.get("/login", ControladorSesion::vistaLogin);
        app.post("/procesarLogin", ControladorSesion::procesarLogin);
        app.get("/logout", ControladorSesion::logout);



        app.get("/productos", ControladorProducto::vistaListar);
        app.get("/admin/crearProducto", ControladorProducto::vistaCrear);
        app.post("/admin/guardarProducto", ControladorProducto::crear);
        app.get("/admin/eliminarProducto/{id}", ControladorProducto::eliminar);
        app.get("/admin/editarProducto/{id}", ControladorProducto::vistaModificar);
        app.post("/admin/actualizarProducto", ControladorProducto::modificar);



        app.get("/carrito", ControladorCarrito::vistaListar);
        app.get("/carrito/agregar/{id}", ControladorCarrito::agregar);
        app.get("/carrito/eliminar/{id}", ControladorCarrito::eliminar);
        app.get("/carrito/vaciar", ControladorCarrito::vaciar);
        app.post("/carrito/procesar", ControladorCarrito::procesar);



        app.get("/usuarios", ControladorUsuario::vistaListar);
        app.get("/admin/crearUsuario", ControladorUsuario::vistaCrear);
        app.post("/admin/guardarUsuario", ControladorUsuario::crear);
        app.get("/admin/editarUsuario/{id}", ControladorUsuario::vistaModificar);
        app.post("/admin/actualizarUsuario", ControladorUsuario::modificar);
        app.get("/admin/eliminarUsuario/{id}", ControladorUsuario::eliminar);



        app.get("/admin/ventas", ControladorVenta::vistaListar);
    }

}