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

    public void guardar(Encuesta encuesta) {
        if (encuesta.getUbicacion() == null) {
            throw new IllegalArgumentException("La ubicación georeferencial es obligatoria.");
        }
        if (encuesta.getFotoBase64() == null || encuesta.getFotoBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("La fotografía en base 64 es obligatoria.");
        }

        if (encuesta.getId() != null && encuesta.getId().trim().isEmpty()) {
            encuesta.setId(null);
        }
        if (encuesta.getId() == null) {
            encuesta.setFechaCreacion(LocalDateTime.now());
        }
        encuesta.setEstadoObjeto(true);
        encuesta.setEstadoSync(EstadoSincronizacion.SINCRONIZADO);
        encuesta.setFechaSincronizacion(LocalDateTime.now());
        encuestaRepository.guardar(encuesta);
    }

    public void modificar(Encuesta encuesta) {
        buscarActivaPorId(encuesta.getId());

        if (encuesta.getUbicacion() == null) {
            throw new IllegalArgumentException("La ubicación georeferencial es obligatoria.");
        }
        if (encuesta.getFotoBase64() == null || encuesta.getFotoBase64().trim().isEmpty()) {
            throw new IllegalArgumentException("La fotografía en base 64 es obligatoria.");
        }

        encuestaRepository.guardar(encuesta);
    }

    public Encuesta buscarPorId(String id) {
        return buscarActivaPorId(id);
    }
    public void desactivar(String id) {
        desactivarEncuesta(id);
    }
    public List<Encuesta> listarTodasActivasPorUsuario(String usuarioId) {
        UsuarioService usuarioService = new UsuarioService();
        if (usuarioId == null || usuarioId.isBlank() || usuarioService.buscarActivoPorId(usuarioId) == null) {
            throw new IllegalArgumentException("El id de usuario proporcionado es inválido o el usuario no existe.");
        }

        return encuestaRepository.listarTodasPorUsuario(usuarioId)
                .stream()
                .filter(Encuesta::isEstadoObjeto)
                .toList();
    }

    public List<Encuesta> listarTodasActivas() {
        return encuestaRepository.listarTodas()
                .stream()
                .filter(Encuesta::isEstadoObjeto)
                .toList();
    }

    public void procesarSincronizacion(List<Encuesta> encuestasPendientes) {
        for (Encuesta encuesta : encuestasPendientes) {
            guardar(encuesta);
        }
    }

    public Encuesta buscarActivaPorId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El id de la encuesta no puede estar vacío.");
        }

        Encuesta encuesta = encuestaRepository.buscarPorId(id);

        if (encuesta == null || !encuesta.isEstadoObjeto()) {
            throw new IllegalArgumentException("La encuesta solicitada no existe o se encuentra inactiva.");
        }

        return encuesta;
    }

    public void desactivarEncuesta(String id) {
        buscarActivaPorId(id);
        encuestaRepository.desactivar(id);
    }
}