package org.example.models;

import java.util.ArrayList;

public class Carrito {
    private long id;
    private ArrayList<Producto> listaProducto;
    private Usuario usuario;
    private EstadoObjeto estado;

    public Carrito(Usuario usuario) {
        this.id = usuario.getId();
        this.listaProducto = new ArrayList<Producto>();
        this.usuario = usuario;
        this.estado = EstadoObjeto.ACTIVO;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ArrayList<Producto> getListaProducto() {
        return listaProducto;
    }

    public void setListaProducto(ArrayList<Producto> listaProducto) {
        this.listaProducto = listaProducto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoObjeto getEstado() {
        return estado;
    }

    public void setEstado(EstadoObjeto estado) {
        this.estado = estado;
    }
}