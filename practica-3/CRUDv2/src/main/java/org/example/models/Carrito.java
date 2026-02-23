package org.example.models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    private Integer id;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "carrito_productos",
            joinColumns = @JoinColumn(name = "carrito_id"),
            inverseJoinColumns = @JoinColumn(name = "producto_id")
    )
    private List<Producto> listaProducto;

    @OneToOne
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private EstadoObjeto estado;


    public Carrito() {
        this.listaProducto = new ArrayList<>();
        this.estado = EstadoObjeto.ACTIVO;
    }


    public Carrito(Usuario usuario) {
        this();
        this.usuario = usuario;
        this.id = usuario.getId();
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Producto> getListaProducto() {
        return listaProducto;
    }

    public void setListaProducto(List<Producto> listaProducto) {
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