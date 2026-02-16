package org.example.models;

import java.math.BigDecimal;
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

    public BigDecimal getTotalVenta() {
        BigDecimal total = BigDecimal.ZERO; //
        for (Producto p : listaProducto) { //

            BigDecimal cantidadBD = new BigDecimal(p.getCantidad());
            BigDecimal subtotal = p.getPrecio().multiply(cantidadBD);
            total = total.add(subtotal); //
        }

        return total;
    }
}
