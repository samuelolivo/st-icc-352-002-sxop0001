package org.example.models;

import java.time.LocalDateTime;


public class Encuesta {

    private String id;
    private String nombre;
    private String sector;
    private NivelEscolar nivelEscolar;
    private Ubicacion ubicacion;
    private String fotoBase64;
    private EstadoSincronizacion estadoSync;
    private String usuarioId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaSincronizacion;


    public Encuesta() {}

    public Encuesta(String nombre, String sector, NivelEscolar nivelEscolar,
                    Ubicacion ubicacion, String fotoBase64, String usuarioId) {
        this.nombre = nombre;
        this.sector = sector;
        this.nivelEscolar = nivelEscolar;
        this.ubicacion = ubicacion;
        this.fotoBase64 = fotoBase64;
        this.usuarioId = usuarioId;
        this.estadoSync = EstadoSincronizacion.PENDIENTE; // Por defecto empieza sin sincronizar
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public NivelEscolar getNivelEscolar() { return nivelEscolar; }
    public void setNivelEscolar(NivelEscolar nivelEscolar) { this.nivelEscolar = nivelEscolar; }

    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public String getFotoBase64() { return fotoBase64; }
    public void setFotoBase64(String fotoBase64) { this.fotoBase64 = fotoBase64; }

    public EstadoSincronizacion getEstadoSync() { return estadoSync; }
    public void setEstadoSync(EstadoSincronizacion estadoSync) { this.estadoSync = estadoSync; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaSincronizacion() { return fechaSincronizacion; }
    public void setFechaSincronizacion(LocalDateTime fechaSincronizacion) {
        this.fechaSincronizacion = fechaSincronizacion;
    }
}
