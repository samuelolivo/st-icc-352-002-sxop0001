package org.example.repository;

import com.mongodb.MongoException;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import dev.morphia.query.filters.Filters;
import dev.morphia.query.updates.UpdateOperators;
import org.example.models.Usuario;
import org.example.services.MongoDBService;

import java.util.List;

public class UsuarioRepository {
    private final Datastore ds;

    public UsuarioRepository() {
        this.ds = MongoDBService.getDatastore();

        if (this.ds == null) {
            throw new IllegalStateException("No se pudo inicializar Datastore");
        }
    }

    public void guardar(Usuario usuario) {
        try {
            ds.save(usuario);
        } catch (MongoException e) {
            throw new RuntimeException("Error guardando usuario", e);
        }
    }

    public void desactivar(String id) {
        try {
            ds.find(Usuario.class)
                    .filter(Filters.eq("_id", id))
                    .update(new UpdateOptions(), UpdateOperators.set("estadoObjeto", false));
        } catch (MongoException e) {
            throw new RuntimeException("Error desactivando usuario", e);
        }
    }

    public Usuario buscarPorId(String id) {
        try {
            return ds.find(Usuario.class)
                    .filter(Filters.eq("_id", id))
                    .first();
        } catch (MongoException e) {
            throw new RuntimeException("Error buscando usuario por ID", e);
        }
    }

    public Usuario buscarPorEmail(String email) {
        try {
            return ds.find(Usuario.class)
                    .filter(Filters.eq("email", email))
                    .first();
        } catch (MongoException e) {
            throw new RuntimeException("Error buscando usuario por email", e);
        }
    }

    public List<Usuario> listarTodo() {
        try {
            return ds.find(Usuario.class)
                    .iterator()
                    .toList();
        } catch (MongoException e) {
            throw new RuntimeException("Error listando usuarios", e);
        }
    }
}