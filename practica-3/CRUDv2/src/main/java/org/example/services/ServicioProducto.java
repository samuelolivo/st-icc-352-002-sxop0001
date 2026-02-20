package org.example.services;

import org.example.models.EstadoObjeto;
import org.example.models.Producto;

import java.math.BigDecimal;
import java.util.ArrayList;


public class ServicioProducto {

    private ArrayList<Producto> listaProductos;

    public ServicioProducto() {
        listaProductos = new ArrayList<Producto>();
    }

    public Producto crear(String nombre, BigDecimal precio, int cantidad) {

        Producto producto = new Producto(nombre, precio,cantidad);

        listaProductos.add(producto);

        return producto;
    }

    public Producto buscarPorId(int id) {

        for (Producto producto : listaProductos) {
            if (producto.getId() == id) {
                return producto;
            }
        }

        return null;
    }

    public Producto buscarActivoPorId(int id) {

        Producto producto = buscarPorId(id);

        if (producto != null && producto.getEstado() == EstadoObjeto.ACTIVO) {
            return producto;
        }

        return null;
    }

    public boolean modificarPorId(int id, String nuevoNombre, BigDecimal nuevoPrecio, int nuevaCantidad) {

        Producto producto = buscarPorId(id);

        if (producto != null && producto.getEstado() == EstadoObjeto.ACTIVO) {
            producto.setNombre(nuevoNombre);
            producto.setPrecio(nuevoPrecio);
            producto.setCantidad(nuevaCantidad);

            return true;
        }

        return false;
    }


    public boolean borrarPorId(int id) {

        Producto producto = buscarPorId(id);

        if (producto != null && producto.getEstado() == EstadoObjeto.ACTIVO) {
            producto.setEstado(EstadoObjeto.INACTIVO);
            return true;
        }

        return false;
    }


    public ArrayList<Producto> listarTodos() {
        return new ArrayList<>(listaProductos);
    }

    public ArrayList<Producto> listarActivos() {

        ArrayList<Producto> activos = new ArrayList<>();

        for (Producto producto : listaProductos) {
            if (producto.getEstado() == EstadoObjeto.ACTIVO) {
                activos.add(producto);
            }
        }

        return activos;
    }
}