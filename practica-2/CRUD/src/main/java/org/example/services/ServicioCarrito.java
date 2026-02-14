package org.example.services;

import org.example.models.Carrito;
import org.example.models.Producto;
import org.example.models.Usuario;

import java.util.ArrayList;

public class ServicioCarrito {

    private ArrayList<Carrito> listaCarritos;

    public ServicioCarrito() {
        this.listaCarritos = new ArrayList<>();
    }

    public Carrito crear(Usuario usuario) {

        Carrito carrito = new Carrito(usuario);
        listaCarritos.add(carrito);

        return carrito;
    }

    public Carrito buscarPorId(long id) {
        for (Carrito c : listaCarritos) {
            if (c.getId() == id) {
                return c;
            }
        }

        return null;
    }

    public boolean agregarProducto(long idCarrito, Producto producto) {
        Carrito carrito = buscarPorId(idCarrito);
        if (carrito == null) {
            return false;
        }

        carrito.getListaProducto().add(producto);
        return true;
    }


    public boolean eliminarProducto(long idCarrito, long idProducto) {

        Carrito carrito = buscarPorId(idCarrito);
        if (carrito == null) {
            return false;
        }

        ArrayList<Producto> productos = carrito.getListaProducto();

        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                productos.remove(p);
                return true;
            }
        }

        return false;
    }

    public ArrayList<Producto> listarProductos(long idCarrito) {

        Carrito carrito = buscarPorId(idCarrito);

        if (carrito == null) {
            return null;
        }

        return carrito.getListaProducto();
    }

    public boolean vaciarCarrito(long idCarrito) {
        Carrito carrito = buscarPorId(idCarrito);

        if (carrito == null) {
            return false;
        }
        carrito.getListaProducto().clear();
        return true;
    }

    public ArrayList<Carrito> listar() {
        return listaCarritos;
    }
}