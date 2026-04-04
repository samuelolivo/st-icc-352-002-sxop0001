package org.example.repository;

import com.mongodb.MongoException;
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

        if (this.ds == null) {
            throw new IllegalStateException("No se pudo inicializar Datastore de MongoDB");
        }
    }

    public void guardar(Encuesta encuesta) {
        try {
            ds.save(encuesta);
        } catch (MongoException e) {
            throw new RuntimeException("Error guardando encuesta", e);
        }
    }

    public void desactivar(String id) {
        try {
            ds.find(Encuesta.class)
                    .filter(Filters.eq("_id", id))
                    .update(new UpdateOptions(), UpdateOperators.set("estadoObjeto", false));
        } catch (MongoException e) {
            throw new RuntimeException("Error desactivando encuesta", e);
        }
    }

    public Encuesta buscarPorId(String id) {
        try {
            return ds.find(Encuesta.class)
                    .filter(Filters.eq("_id", id))
                    .first();
        } catch (MongoException e) {
            throw new RuntimeException("Error buscando encuesta por ID", e);
        }
    }

    public List<Encuesta> listarTodasPorUsuario(String usuarioId) {
        try {
            return ds.find(Encuesta.class)
                    .filter(Filters.eq("usuarioId", usuarioId))
                    .iterator().toList();
        } catch (MongoException e) {
            throw new RuntimeException("Error listando encuestas por usuario", e);
        }
    }

    public List<Encuesta> listarTodas() {
        try {
            return ds.find(Encuesta.class)
                    .iterator()
                    .toList();
        } catch (MongoException e) {
            throw new RuntimeException("Error listando encuestas", e);
        }
    }
}