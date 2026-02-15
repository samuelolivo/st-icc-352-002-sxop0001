package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.example.models.Producto;
import org.example.models.RolesUsuario;
import org.example.models.Usuario;
import org.example.models.Venta;
import org.example.services.ServicioCarrito;
import org.example.services.ServicioProducto;
import org.example.services.ServicioVenta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class Main {
    static enum KeySession {
        USUARIO
    }

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


        app.before("/**", ctx -> {
            System.out.println(ctx.path());

            if (ctx.path().startsWith("/login.html") ||
                    ctx.path().startsWith("/procesarLogin") ||
                    ctx.path().startsWith("/productos") ||
                    ctx.path().startsWith("/productos/admin/") ||
                    ctx.path().startsWith("/carrito/"))
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

        app.get("/admin/eliminarProducto/{id}", ctx -> {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Main.servicioProducto.borrarPorId(id);
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

        app.get("/carrito/agregar/{id}", ctx -> {
            int idProducto = Integer.parseInt(ctx.pathParam("id"));
            Producto producto = servicioProducto.buscarActivoPorId(idProducto);
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());


            if (producto != null && producto.getCantidad() > 0) {
                if (servicioCarrito.buscarPorId(usuario.getId()) == null) {
                    servicioCarrito.crear(usuario);
                }

                servicioCarrito.agregarProducto(usuario.getId(), producto);
                ctx.redirect("/productos");
            } else {
                ctx.result("Lo sentimos, no hay stock suficiente de este producto.");
            }
        });


        app.get("/carrito", ctx -> {
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            ArrayList<Producto> productosCarrito = servicioCarrito.listarProductos(usuario.getId());

            if (productosCarrito == null) productosCarrito = new ArrayList<>();

            BigDecimal total = productosCarrito.stream()
                    .map(Producto::getPrecio)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String error = ctx.sessionAttribute("errorStock");
            ctx.consumeSessionAttribute("errorStock");

            Map<String, Object> model = new HashMap<>();
            model.put("productos", productosCarrito);
            model.put("total", total);
            model.put("usuario", usuario);
            model.put("mensajeError", error);

            ctx.render("templates/carrito.html", model);
        });


        app.get("/carrito/eliminar/{id}", ctx -> {
            long idProd = Long.parseLong(ctx.pathParam("id"));
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            servicioCarrito.eliminarProducto(usuario.getId(), idProd);
            ctx.redirect("/carrito");
        });

        app.post("/carrito/procesar", ctx -> {
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            String nombreCliente = ctx.formParam("nombreCliente");
            ArrayList<Producto> enCarrito = servicioCarrito.listarProductos(usuario.getId());

            if (enCarrito == null || enCarrito.isEmpty()) {
                ctx.redirect("/carrito");
                return;
            }
            for (Producto item : enCarrito) {
                long cantidadPedida = enCarrito.stream()
                        .filter(p -> p.getId() == item.getId()).count();

                Producto stockReal = servicioProducto.buscarActivoPorId((int) item.getId());

                if (stockReal == null || cantidadPedida > stockReal.getCantidad()) {
                    int disponibles = (stockReal != null) ? stockReal.getCantidad() : 0;
                    ctx.sessionAttribute("errorStock", "No hay suficiente stock para '" + item.getNombre() +
                            "'. Quedan " + disponibles + ".");

                    ctx.redirect("/carrito");
                    return;
                }
            }

            for (Producto item : enCarrito) {
                Producto stockReal = servicioProducto.buscarActivoPorId((int) item.getId());
                servicioProducto.modificarPorId(
                        (int)item.getId(),
                        stockReal.getNombre(),
                        stockReal.getPrecio(),
                        stockReal.getCantidad() - 1
                );
            }


            servicioVenta.registrar(nombreCliente, enCarrito);
            servicioCarrito.vaciarCarrito(usuario.getId());

            ctx.redirect("/productos");
        });
    }

}