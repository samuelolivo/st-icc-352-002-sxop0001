package org.example.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inscripciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"evento_id", "usuario_id"})
})
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evento_id", nullable = false)
    private Evento evento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_inscripcion")
    private LocalDateTime fechaInscripcion;

    private boolean asistio = false;

    @Column(name = "fecha_asistencia")
    private LocalDateTime fechaAsistencia;

    @Column(name = "token_qr", unique = true)
    private String tokenQr;

    public Inscripcion() {
    }

    public Inscripcion(Evento evento, Usuario usuario) {
        this.evento = evento;
        this.usuario = usuario;
        this.fechaInscripcion = LocalDateTime.now();
        this.tokenQr = UUID.randomUUID().toString();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Evento getEvento() { return evento; }
    public void setEvento(Evento evento) { this.evento = evento; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public boolean isAsistio() { return asistio; }
    public void setAsistio(boolean asistio) { this.asistio = asistio; }

    public String getTokenQr() { return tokenQr; }
    public void setTokenQr(String tokenQr) { this.tokenQr = tokenQr; }

    public LocalDateTime getFechaAsistencia() { return fechaAsistencia; }
    public void setFechaAsistencia(LocalDateTime fechaAsistencia) { this.fechaAsistencia = fechaAsistencia; }
}