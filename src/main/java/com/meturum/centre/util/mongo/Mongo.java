package com.meturum.centre.util.mongo;

import com.meturum.centra.Centra;
import com.meturum.centra.mongo.IMongo;
import com.meturum.centre.Centre;
import com.meturum.centre.util.SystemImpl;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.Document;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class Mongo extends SystemImpl implements IMongo {

    private MongoClient client;

    public Mongo(@NotNull Centre centre) {
        super(centre);

        Logger logger = Logger.getLogger("org.mongodb.driver");
        logger.setLevel(Level.SEVERE);

        ConnectionString uri = new ConnectionString("mongodb+srv://ijyrs:6CzcFqU7t8m9mvr@ijyrs.dhhdn.mongodb.net/?keepAlive=true&retryWrites=true&w=majority");

        try {
            this.client = MongoClients.create(uri);
            MongoDatabase gdb = client.getDatabase("global");

            // Test the connection.
            gdb.runCommand(new BsonDocument("ping", new BsonInt64(1)));
        } catch (Exception ignored) { }
    }

    /**
     * @param name The name of the collection.
     * @param type The type of the collection.
     * @return The collection.
     */
    public @NotNull CollectionWrapperImpl getCollection(@NotNull String name, @NotNull MongoClientTypes type) {
        MongoCollection<Document> collection = switch (type) {
            case GLOBAL_DATABASE -> getDatabase("global").getCollection(name);
            case SERVER_DATABASE -> getDatabase("s1-NA").getCollection(name);
        };

        return new CollectionWrapperImpl((Plugin) centre, collection);
    }

    private MongoDatabase getDatabase(String name) {
        return client.getDatabase(name);
    }

}
