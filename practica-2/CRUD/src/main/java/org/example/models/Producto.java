package org.example.models;

import java.math.BigDecimal;

public class Producto {
    private static int count;
    private int id;
    private String nombre;
    private BigDecimal precio;
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
}
