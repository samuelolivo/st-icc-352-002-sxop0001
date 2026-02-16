package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
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

    static enum KeySession {
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


        app.before("/**", ctx -> {
            System.out.println(ctx.path());
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            Boolean check = ctx.sessionAttribute("primera_vez");

            if (check == null) {
                ctx.sessionAttribute(KeySession.USUARIO.name(), servicioUsuario.buscarPorUsername(""));
                ctx.sessionAttribute("primera_vez", true);
                ctx.redirect("/productos");
            }
        });

        app.before("/admin/**", ctx -> {
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());

            if (usuario == null || usuario.getRol() != RolesUsuario.ADMIN) {
                String anteriorPath = ctx.header("Referer");
                ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
            }
        });

        app.get("/", ctx -> {
            ctx.redirect("/productos");
        });

        app.get("/login", ctx -> {
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            Map<String, Object> model = new HashMap<>();

            String guardado = ctx.sessionAttribute(KeySession.REFERER.name());
            String actual = ctx.header("Referer");

            if (guardado == null || guardado.isEmpty()) {
                if (actual != null && !actual.contains("/login") && !actual.contains("/procesarLogin")) {
                    ctx.sessionAttribute(KeySession.REFERER.name(), actual);
                }
            }

            if (ctx.queryParam("error") != null) {
                model.put("error", "Usuario o contraseña incorrectos.");
            }

            ctx.render("templates/login.html", model);
        });

        app.post("/procesarLogin", ctx -> {
            String nombre = ctx.formParam("usuario");
            String password = ctx.formParam("password");

            Usuario usuario = servicioUsuario.validarLogin(nombre, password);

            if (usuario != null) {
                ctx.sessionAttribute(KeySession.USUARIO.name(), usuario);
                String anteriorPath = ctx.sessionAttribute(KeySession.REFERER.name());
                ctx.redirect((anteriorPath != null) ? anteriorPath : "/productos");
            } else {
                ctx.redirect("/login?error=1");
            }
        });

        app.get("/logout", ctx -> {
            ctx.sessionAttribute(KeySession.USUARIO.name(), servicioUsuario.buscarPorUsername(""));

            String anteriorPath = ctx.header("Referer");
            ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
        });

        app.get("/volver", ctx -> {
            String anteriorPath = ctx.sessionAttribute(KeySession.REFERER.name());
            ctx.redirect((anteriorPath != null && !anteriorPath.isEmpty()) ? anteriorPath : "/productos");
        });


        app.get("/productos", ctx -> {
            Usuario usuarioLogueado = ctx.sessionAttribute(KeySession.USUARIO.name());
            Map<String, Object> model = new HashMap<>();

            int cantidadCarrito = 0;
            if (usuarioLogueado != null) {
                ArrayList<Producto> lista = servicioCarrito.listarProductos(usuarioLogueado.getId());
                if (lista != null) {
                    cantidadCarrito = lista.size();
                }
            }

            model.put("productos", servicioProducto.listarActivos());
            model.put("usuario", usuarioLogueado);
            model.put("cantidadCarrito", cantidadCarrito);
            ctx.sessionAttribute(KeySession.REFERER.name(), "/productos");
            ctx.render("templates/productos.html", model);
        });

        app.get("/admin/crearProducto", ctx -> {
            ctx.render("templates/admin/crearProducto.html");
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
                ctx.render("templates/admin/editarProducto.html", model);
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

        app.get("/carrito/vaciar", ctx -> {
            Usuario usuario = ctx.sessionAttribute(KeySession.USUARIO.name());
            String nombreCliente = ctx.formParam("nombreCliente");
            ArrayList<Producto> enCarrito = servicioCarrito.listarProductos(usuario.getId());

            if (enCarrito == null || enCarrito.isEmpty()) {
                ctx.redirect("/carrito");
                return;
            }

            servicioCarrito.vaciarCarrito(usuario.getId());
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

        app.get("/usuarios", ctx -> {
            Usuario usuarioLogueado = ctx.sessionAttribute(KeySession.USUARIO.name());

            Map<String, Object> model = new HashMap<>();

            int cantidadCarrito = 0;
            if (usuarioLogueado != null) {
                ArrayList<Producto> lista = servicioCarrito.listarProductos(usuarioLogueado.getId());
                if (lista != null) {
                    cantidadCarrito = lista.size();
                }
            }

            model.put("usuarios", servicioUsuario.listarActivos());
            model.put("usuario", usuarioLogueado);
            model.put("cantidadCarrito", cantidadCarrito);
            ctx.sessionAttribute(KeySession.REFERER.name(), "/usuarios");
            ctx.render("templates/usuarios.html", model);
        });

        app.get("/admin/crearUsuario", ctx -> {
            Map<String, Object> model = new HashMap<>();
            model.put("roles", RolesUsuario.rolesSeleccionables());

            if (ctx.queryParam("error") != null) {
                if (ctx.queryParam("error").equals("1")) model.put("error", "El nombre de usuario está en uso.");
                if (ctx.queryParam("error").equals("2")) model.put("error", "Las contraseñas no coinciden.");
            }

            ctx.render("templates/admin/crearUsuario.html", model);
        });

        app.post("/admin/guardarUsuario", ctx -> {

            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");
            String passwordConfirm = ctx.formParam("passwordConfirm");
            String rolStr = ctx.formParam("rol");

            if (servicioUsuario.buscarPorUsername(usuario) != null)
            {
                ctx.redirect("/admin/crearUsuario?error=1");
                return;
            }

            if (!password.equals(passwordConfirm)) {
                ctx.redirect("/admin/crearUsuario?error=2");
                return;
            }

            RolesUsuario rol = RolesUsuario.valueOf(rolStr);

            servicioUsuario.crear(usuario, password, rol);
            ctx.redirect("/usuarios");
        });

        app.get("/admin/editarUsuario/{id}", ctx -> {

            String username = ctx.pathParam("id");
            Usuario usuario = servicioUsuario.buscarPorUsername(username);

            if (usuario != null) {
                Map<String, Object> model = new HashMap<>();
                model.put("usuarioEditar", usuario);
                model.put("roles", RolesUsuario.rolesSeleccionables());
                ctx.render("templates/admin/editarUsuario.html", model);
            }
            else {
                ctx.redirect("/usuarios");
            }
        });

        app.post("/admin/actualizarUsuario", ctx -> {

            int id = Integer.parseInt(ctx.formParam("id"));
            String usuario = ctx.formParam("usuario");
            String password = ctx.formParam("password");
            String rolStr = ctx.formParam("rol");
            RolesUsuario rol = RolesUsuario.valueOf(rolStr);

            Usuario userEdit = servicioUsuario.buscarPorId(id);
            if (userEdit != null && userEdit.getUsuario().equals("admin"))
            {
                ctx.redirect("/usuarios");
                return;
            }
            servicioUsuario.modificarPorId(id, usuario, password, rol);
            ctx.redirect("/usuarios");
        });

        app.get("/admin/eliminarUsuario/{id}", ctx -> {

            String username = ctx.pathParam("id");
            if (username.equals("admin")) {
                ctx.redirect("/usuarios");
                return;
            }
            Main.servicioUsuario.borrarPorUsername(username);
            ctx.redirect("/usuarios");

        });

        app.get("/admin/ventas", ctx -> {
            Usuario usuarioLogueado = ctx.sessionAttribute(KeySession.USUARIO.name());
            Map<String, Object> model = new HashMap<>();

            model.put("ventas", servicioVenta.listarTodas());
            model.put("usuario", usuarioLogueado);

            ctx.render("templates/admin/ventas.html", model);
        });
    }

}