package org.example.services;

import org.example.models.Encuesta;
import org.example.models.EstadoSincronizacion;
import org.example.repository.EncuestaRepository;
import java.time.LocalDateTime;
import java.util.List;

public class EncuestaService {

    private final EncuestaRepository encuestaRepository;

    public EncuestaService() {
        this.encuestaRepository = new EncuestaRepository();
    }

    // Requerimiento 16.2: Creación de formulario incluyendo imagen en base 64
    public void crearEncuesta(Encuesta encuesta) {
        // Lógica de validación puntual del documento
        if (encuesta.getUbicacion() == null) {
            throw new IllegalArgumentException("La ubicación georeferencial es obligatoria.");
        }
        if (encuesta.getFotoBase64() == null || encuesta.getFotoBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("La fotografía en base 64 es obligatoria.");
        }

        encuestaRepository.guardar(encuesta);
    }

    public List<Encuesta> listarPorUsuario(String usuarioId) {
        return encuestaRepository.listarPorUsuario(usuarioId);
    }

    public List<Encuesta> listarTodas() {
        return encuestaRepository.listarTodo();
    }

    public void procesarSincronizacion(List<Encuesta> encuestasPendientes) {
        for (Encuesta encuesta : encuestasPendientes) {

            encuesta.setEstadoSync(EstadoSincronizacion.SINCRONIZADO);
            encuesta.setFechaSincronizacion(LocalDateTime.now());

            encuesta.setEstadoObjeto(true);

            encuestaRepository.guardar(encuesta);
        }
    }

    public Encuesta buscarPorId(String id) {
        return encuestaRepository.buscarPorId(id);
    }

    public void desactivarEncuesta(String id) {
        encuestaRepository.desactivar(id);
    }
}