package org.example.models;

import java.util.ArrayList;

public class Carrito {
    private static long count;
    private long id;
    private ArrayList<Producto> listaProducto;
    private Usuario usuario;
    private EstadoObjeto estado;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Producto> getListaProducto() {
        return listaProducto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public static long getCount() {
        return count;
    }

    public static void setCount(long count) {
        Carrito.count = count;
    }

    public EstadoObjeto getEstado() {
        return estado;
    }

    public void setEstado(EstadoObjeto estado) {
        this.estado = estado;
    }
}