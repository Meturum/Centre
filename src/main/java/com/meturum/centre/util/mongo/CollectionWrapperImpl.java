package com.meturum.centre.util.mongo;

import com.meturum.centra.mongo.CollectionWrapper;
import com.meturum.centre.util.mongo.tasks.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class CollectionWrapperImpl implements CollectionWrapper {

    private final MongoCollection<Document> raw;
    private final Plugin plugin;

    public CollectionWrapperImpl(@NotNull Plugin plugin, @NotNull MongoCollection<Document> raw) {
        this.plugin = plugin;
        this.raw = raw;
    }

    public void findAsync(@Nullable Bson filter, @NotNull FindLambda lambda) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new FindTask(raw, filter, lambda)
        );
    }

    @Override
    public void findAsync(@NotNull FindLambda lambda) {
        findAsync(null, lambda);
    }

    @Override
    public void insertOneAsync(@NotNull Document document, @Nullable InsertOneLambda lambda) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new InsertOneTask(raw, document, lambda)
        );
    }

    public void insertOneAsync(@NotNull Document document) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new InsertOneTask(raw, document)
        );
    }

    public void updateOneAsync(@NotNull Bson filter, @NotNull Bson updates, @Nullable UpdateOptions options, @Nullable UpdateOneLambda lambda) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new UpdateOneTask(raw, filter, updates, options, lambda)
        );
    }

    public void updateOneAsync(@NotNull Bson filter, @NotNull Bson updates, @Nullable UpdateOneLambda lambda) {
        updateOneAsync(filter, updates, null, lambda);
    }

    @Override
    public void updateOneAsync(@NotNull Bson filter, @NotNull Bson updates, @Nullable UpdateOptions options) {
        updateOneAsync(filter, updates, options, null);
    }

    @Override
    public void updateOneAsync(@NotNull Bson filter, @NotNull Bson updates) {
        updateOneAsync(filter, updates, null, null);
    }

    public void replaceOneAsync(@NotNull Bson filter, @NotNull Document updates, @Nullable ReplaceOptions options, @Nullable ReplaceOneLambda lambda) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new ReplaceOneTask(raw, filter, updates, options, lambda)
        );
    }

    public void replaceOneAsync(@NotNull Bson filter, @NotNull Document updates, @Nullable ReplaceOneLambda lambda) {
        replaceOneAsync(filter, updates, null, lambda);
    }

    public void replaceOneAsync(@NotNull Bson filter, @NotNull Document updates, @Nullable ReplaceOptions options) {
        replaceOneAsync(filter, updates, options, null);
    }

    public void replaceOneAsync(@NotNull Bson filter, @NotNull Document updates) {
        replaceOneAsync(filter, updates, null, null);
    }

    public void deleteOneAsync(@NotNull Bson filter, @Nullable DeleteOneLambda lambda) {
        plugin.getServer().getScheduler().runTaskAsynchronously(
                plugin,
                new DeleteOneTask(raw, filter, lambda)
        );
    }

    public void deleteOneAsync(@NotNull Bson filter) {
        deleteOneAsync(filter, null);
    }

    @Override
    public @NotNull MongoCollection<Document> raw() {
        return raw;
    }

}

