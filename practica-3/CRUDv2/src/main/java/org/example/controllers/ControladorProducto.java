package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Producto;
import org.example.models.Usuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.example.Main.servicioCarrito;
import static org.example.Main.servicioProducto;

public class ControladorProducto {
    public static void vistaListar(Context ctx){
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());
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
        ctx.sessionAttribute(Main.KeySession.REFERER.name(), "/productos");
        ctx.render("templates/productos.html", model);
    }

    public static void vistaCrear(Context ctx){
        ctx.render("templates/admin/crearProducto.html");
    }

    public static void crear(Context ctx){
        String nombre = ctx.formParam("nombre");
        String precioStr = ctx.formParam("precio");
        String cantidadStr = ctx.formParam("cantidad");

        if (nombre != null && precioStr != null) {
            BigDecimal precio = new BigDecimal(precioStr);

            assert cantidadStr != null;
            servicioProducto.crear(nombre, precio, Integer.parseInt(cantidadStr));
        }
        ctx.redirect("/productos");
    }

    public static void eliminar(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        servicioProducto.borrarPorId(id);
        ctx.redirect("/productos");
    }


    public static void vistaModificar(Context ctx){
        int id = Integer.parseInt(ctx.pathParam("id"));
        Producto producto = servicioProducto.buscarActivoPorId(id);

        if (producto != null) {
            Map<String, Object> model = new HashMap<>();
            model.put("producto", producto);
            ctx.render("templates/admin/editarProducto.html", model);
        } else {
            ctx.redirect("/productos");
        }
    }

    public static void modificar(Context ctx){
        int id = Integer.parseInt(ctx.formParam("id"));
        String nombre = ctx.formParam("nombre");
        BigDecimal precio = new BigDecimal(ctx.formParam("precio"));
        int cantidad = Integer.parseInt(ctx.formParam("cantidad"));

        servicioProducto.modificarPorId(id, nombre, precio, cantidad);
        ctx.redirect("/productos");
    }
}
