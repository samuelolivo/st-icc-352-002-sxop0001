package org.example.repository;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import org.example.models.Encuesta;
import org.example.services.MongoDBService;
import java.util.List;

public class EncuestaRepository {
    private final Datastore ds;

    public EncuestaRepository() {
        this.ds = MongoDBService.getDatastore();
    }

    public void guardar(Encuesta encuesta) {
        ds.save(encuesta);
    }

    public void desactivar(String id) {
        ds.find(Encuesta.class)
                .filter(Filters.eq("_id", id))
                .update(
                        new UpdateOptions(),
                        UpdateOperators.set("estadoObjeto", false)
                );
    }

    public Encuesta buscarPorId(String id) {
        return ds.find(Encuesta.class)
                .filter(Filters.eq("_id", id))
                .first();
    }

    public List<Encuesta> listarPorUsuario(String usuarioId) {
        return ds.find(Encuesta.class)
                .filter(Filters.eq("usuarioId", usuarioId))
                .iterator().toList();
    }

    public List<Encuesta> listarTodas() {
        return ds.find(Encuesta.class).iterator().toList();
    }
}