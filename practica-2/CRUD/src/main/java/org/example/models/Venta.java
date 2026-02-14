package org.example.models;

import java.util.ArrayList;
import java.util.Date;

public class Venta {
    private static long count;
    private long id;
    private Date fecha;
    private String userCliente;
    private ArrayList<Producto> listaProducto;

    public Venta(Date fecha, String userCliente) {
        this.id = getCount();
        this.fecha = fecha;
        this.userCliente = userCliente;
        this.listaProducto = new ArrayList<Producto>();

        setCount(1 + getCount());
    }

    public static long getCount() {
        return count;
    }

    public static void setCount(long count) {
        Venta.count = count;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getUserCliente() {
        return userCliente;
    }

    public void setUserCliente(String userCliente) {
        this.userCliente = userCliente;
    }

    public ArrayList<Producto> getListaProducto() {
        return listaProducto;
    }

    public void setListaProducto(ArrayList<Producto> listaProducto) {
        this.listaProducto = listaProducto;
    }
}
