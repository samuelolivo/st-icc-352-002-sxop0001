package org.example.services;

import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MongoDBService {
    private static Datastore datastore;

    public static Datastore getDatastore() {
        if (datastore == null) {
            // cambiar luego
            var mongoClient = MongoClients.create("mongodb+srv://tu_usuario:tu_password@cluster...");
            datastore = Morphia.createDatastore(mongoClient);
        }
        return datastore;
    }
}
