package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.models.Producto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;
import org.example.models.Venta;
import org.example.services.ServicioProducto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static enum KeySession {
        USUARIO
    }

    public static ServicioProducto servicioProducto = new ServicioProducto();


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

        // Mantenemos tu filtro tal cual lo pediste
        app.before("/**", ctx -> {
            System.out.println(ctx.path());

            if (ctx.path().startsWith("/login.html") ||
                    ctx.path().startsWith("/procesarLogin") ||
                    ctx.path().startsWith("/productos") ||
                    ctx.path().startsWith("/productos/admin/"))
                    {
                return;
            }
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            if (usuario == null) {
                ctx.redirect("/login.html");
            }
        });

        app.post("/procesarLogin", ctx -> {
            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");

            if ("admin".equals(usuario) && "admin".equals(password)) {
                Usuario user = new Usuario(usuario, password, RolesUsuario.ADMIN);
                ctx.sessionAttribute(KeySession.USUARIO.name(), user);
                ctx.redirect("/productos");
            } else {
                ctx.status(401).result("Usuario incorrecto, ingrese un usuario valido.");
            }
        });

        app.get("/productos", ctx -> {
            Usuario usuarioLogueado = ctx.sessionAttribute(KeySession.USUARIO.name());

            Map<String, Object> model = new HashMap<>();

            model.put("productos", servicioProducto.listarActivos());
            model.put("usuario", usuarioLogueado);
            ctx.render("templates/productos.html", model);
        });

        app.get("/admin/crearProducto", ctx -> {
            ctx.render("templates/crearProducto.html");
        });

        app.post("/admin/guardarProducto", ctx -> {
            String nombre = ctx.formParam("nombre");
            String precioStr = ctx.formParam("precio");
            String cantidadStr = ctx.formParam("cantidad");

            if (nombre != null && precioStr != null) {
                BigDecimal precio = new BigDecimal(precioStr);

                assert cantidadStr != null;
                servicioProducto.crear(nombre, precio, Integer.parseInt(cantidadStr));
            }
            ctx.redirect("/productos");
        });
        // --- ELIMINAR ---
        app.get("/admin/eliminarProducto/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Main.servicioProducto.borrarPorId(id); // Usa tu lógica de pasar a INACTIVO
            ctx.redirect("/productos");
        });


        app.get("/admin/editarProducto/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Producto producto = Main.servicioProducto.buscarActivoPorId(id);

            if (producto != null) {
                Map<String, Object> model = new HashMap<>();
                model.put("producto", producto);
                ctx.render("templates/editarProducto.html", model);
            } else {
                ctx.redirect("/productos");
            }
        });

        app.post("/admin/actualizarProducto", ctx -> {
            int id = Integer.parseInt(ctx.formParam("id"));
            String nombre = ctx.formParam("nombre");
            BigDecimal precio = new BigDecimal(ctx.formParam("precio"));
            int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

            Main.servicioProducto.modificarPorId(id, nombre, precio, cantidad);
            ctx.redirect("/productos");
        });
    }
}