package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Producto;
import org.example.models.Usuario;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import static org.example.Main.*;

public class ControladorCarrito {

    public static void vistaListar(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        List<Producto> productosCarrito = servicioCarrito.listarProductos(usuario.getId());

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

    public static void agregar(Context ctx) {
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

            ctx.sessionAttribute("errorStock", "Lo sentimos, el producto seleccionado no tiene stock.");
            ctx.redirect("/productos");
        }
    }

    public static void eliminar(Context ctx) {
        long idProd = Long.parseLong(ctx.pathParam("id"));
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        servicioCarrito.eliminarProducto(usuario.getId(), idProd);
        ctx.redirect("/carrito");
    }

    public static void vaciar(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        servicioCarrito.vaciarCarrito(usuario.getId());
        ctx.redirect("/carrito");
    }

    public static void procesar(Context ctx) {
        Usuario usuario = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
        String nombreCliente = ctx.formParam("nombreCliente");
        List<Producto> enCarrito = servicioCarrito.listarProductos(usuario.getId());

        if (enCarrito == null || enCarrito.isEmpty()) {
            ctx.redirect("/carrito");
            return;
        }


        for (Producto item : enCarrito) {
            long cantidadEnCarrito = enCarrito.stream()
                    .filter(p -> p.getId() == item.getId()).count();

            Producto stockReal = servicioProducto.buscarActivoPorId(item.getId());

            if (stockReal == null || stockReal.getCantidad() < cantidadEnCarrito) {
                ctx.sessionAttribute("errorStock", "No hay stock suficiente para: " + item.getNombre());
                ctx.redirect("/carrito");
                return;
            }
        }


        for (Producto item : enCarrito) {
            Producto stockReal = servicioProducto.buscarActivoPorId(item.getId());
            servicioProducto.modificarPorId(
                    item.getId(),
                    stockReal.getNombre(),
                    stockReal.getPrecio(),
                    stockReal.getCantidad() - 1,
                    stockReal.getDescripcion(),
                    stockReal.getImagenes()
            );
        }


        servicioVenta.registrar(nombreCliente, enCarrito);
        servicioCarrito.vaciarCarrito(usuario.getId());

        ctx.redirect("/productos");
    }
}