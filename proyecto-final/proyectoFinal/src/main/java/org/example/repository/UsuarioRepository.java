package org.example.repository;

import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import org.example.models.Encuesta;
import org.example.models.Usuario;
import org.example.services.MongoDBService;

public class UsuarioRepository {
    private final Datastore ds;

    public UsuarioRepository() {
        this.ds = MongoDBService.getDatastore();
    }

    public void guardar(Usuario usuario) {
        ds.save(usuario);
    }

    public void desactivar(String id) {
        ds.find(Usuario.class)
                .filter(Filters.eq("_id", id))
                .update(
                        new UpdateOptions(),
                        UpdateOperators.set("estadoObjeto", false)
                );
    }

    public Usuario buscarPorId(String id) {
        return ds.find(Usuario.class)
                .filter(Filters.eq("_id", id))
                .first();
    }

    public Usuario buscarPorEmail(String email) {
        return ds.find(Usuario.class)
                .filter(Filters.eq("email", email))
                .first();
    }
}