package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Producto;
import org.example.models.Usuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.example.Main.*;

public class ControladorCarrito {
        public static void vistaListar(Context ctx){
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
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
    }

    public static void agregar(Context ctx){
        int idProducto = Integer.parseInt(ctx.pathParam("id"));
        Producto producto = servicioProducto.buscarActivoPorId(idProducto);
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());


        if (producto != null && producto.getCantidad() > 0) {
            if (servicioCarrito.buscarPorId(usuario.getId()) == null) {
                servicioCarrito.crear(usuario);
            }

            servicioCarrito.agregarProducto(usuario.getId(), producto);
            ctx.redirect("/productos");
        } else {
            ctx.result("Lo sentimos, no hay stock suficiente de este producto.");
        }
    }

    public static void eliminar(Context ctx){
        long idProd = Long.parseLong(ctx.pathParam("id"));
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        servicioCarrito.eliminarProducto(usuario.getId(), idProd);
        ctx.redirect("/carrito");
    }

    public static void vaciar(Context ctx){
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        String nombreCliente = ctx.formParam("nombreCliente");
        ArrayList<Producto> enCarrito = servicioCarrito.listarProductos(usuario.getId());

        if (enCarrito == null || enCarrito.isEmpty()) {
            ctx.redirect("/carrito");
            return;
        }

        servicioCarrito.vaciarCarrito(usuario.getId());
        ctx.redirect("/carrito");
    }

    public static void procesar(Context ctx){
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
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
    }
}
