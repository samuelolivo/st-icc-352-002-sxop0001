package org.example.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    private String userCliente;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "venta_productos",
            joinColumns = @JoinColumn(name = "venta_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> listaProducto;


    public Venta() {
        this.listaProducto = new ArrayList<>();
        this.fecha = new Date();
    }


    public Venta(Date fecha, String userCliente) {
        this();
        this.fecha = fecha;
        this.userCliente = userCliente;
    }



    public BigDecimal getTotalVenta() {
        BigDecimal total = BigDecimal.ZERO;
        for (Producto p : listaProducto) {

            //BigDecimal cantidadBD = new BigDecimal(p.getCantidad());
            //BigDecimal subtotal = p.getPrecio().multiply(cantidadBD);
            total = total.add(p.getPrecio());
        }
        return total;
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

    public List<Producto> getListaProducto() {
        return listaProducto;
    }

    public void setListaProducto(List<Producto> listaProducto) {
        this.listaProducto = listaProducto;
    }
}