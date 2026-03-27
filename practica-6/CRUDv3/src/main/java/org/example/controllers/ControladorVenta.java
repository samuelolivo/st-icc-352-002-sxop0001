package org.example.controllers;

import io.javalin.http.Context;
import org.example.Main;
import org.example.models.Producto;
import org.example.models.Usuario;
import org.example.models.Venta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.Main.servicioVenta;
import static org.example.Main.servicioCarrito;

public class ControladorVenta {

    public static void vistaListar(Context ctx) {
        Usuario usuarioLogueado = ctx.sessionAttribute(Main.KeySession.USUARIO.name());

        List<Venta> listaVentas = servicioVenta.listarTodas();
        int totalVentas = listaVentas.size();

        Map<String, Integer> productosVendidos = new HashMap<>();
        for (Venta v : listaVentas) {
            for (Producto p : v.getListaProducto()) {
                productosVendidos.put(
                        p.getNombre(),
                        productosVendidos.getOrDefault(p.getNombre(), 0) + 1
                );
            }
        }

        int cantidadCarrito = 0;
        if (usuarioLogueado != null) {
            var productos = servicioCarrito.listarProductos(usuarioLogueado.getId());
            if (productos != null) {
                cantidadCarrito = productos.size();
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("ventas", listaVentas);
        model.put("usuario", usuarioLogueado);
        model.put("cantidadCarrito", cantidadCarrito);
        model.put("totalVentasDashboard", totalVentas);
        model.put("productosVendidosDashboard", productosVendidos);

        ctx.render("templates/admin/ventas.html", model);
    }
}