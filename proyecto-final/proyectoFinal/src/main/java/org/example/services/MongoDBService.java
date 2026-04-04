package org.example.services;

import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;

public class MongoDBService {
    private static Datastore datastore;

    public static Datastore getDatastore() {
        if (datastore == null) {
            var mongoClient = MongoClients.create("mongodb+srv://admin:admin@cluster0.yrwk11e.mongodb.net/?appName=Cluster0");
            datastore = Morphia.createDatastore(mongoClient);
        }
        return datastore;
    }
}