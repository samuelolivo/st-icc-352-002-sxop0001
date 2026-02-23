package org.example.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "comentarios")

public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenido;

    @Column(nullable = false)
    private String usuario;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaPublicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    @Enumerated(EnumType.STRING)
    private EstadoObjeto estado;

    public Comentario() {
        this.estado = EstadoObjeto.ACTIVO;
        this.fechaPublicacion = new Date();
    }

    public Comentario(String contenido, String usuario, Producto producto) {
        this();
        this.contenido = contenido;
        this.usuario = usuario;
        this.producto = producto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Date getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Date fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public EstadoObjeto getEstado() { return estado; }

    public void setEstado(EstadoObjeto estado) { this.estado = estado; }
}