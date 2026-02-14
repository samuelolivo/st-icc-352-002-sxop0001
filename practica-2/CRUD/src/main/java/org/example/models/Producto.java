package org.example.models;

import java.math.BigDecimal;

public class Producto {
    private static int count = 0;
    private int id;
    private String nombre;
    private BigDecimal precio;
    private EstadoObjeto estado;

    public Producto(String nombre, BigDecimal precio) {
        this.id = getCount();
        this.nombre = nombre;
        this.precio = precio;
        this.estado = EstadoObjeto.ACTIVO;

        setCount(1 + getCount());
    }

    public static int getCount() {
        return count;
    }

    public static void setCount(int count) {
        Producto.count = count;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public EstadoObjeto getEstado() {
        return estado;
    }

    public void setEstado(EstadoObjeto estado) {
        this.estado = estado;
    }
}
