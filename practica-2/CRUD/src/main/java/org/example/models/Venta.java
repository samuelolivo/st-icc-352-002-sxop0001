package org.example.models;

import java.util.Date;

public class Venta {
    private long id;
    private Date fecha;
    private String userCliente;
    private ArrayList <Producto>;

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
}
