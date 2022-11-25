package com.meturum.centre.util;

import com.google.common.base.Preconditions;
import com.meturum.centra.conversions.Documentable;
import com.meturum.centra.conversions.IDynamicTag;
import com.meturum.centra.conversions.annotations.DocumentableMethod;
import com.meturum.centra.mongo.CollectionWrapper;
import com.meturum.centra.mongo.Mongo;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.util.mongo.MongoImpl;
import com.meturum.centre.util.mongo.CollectionWrapperImpl;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Creates a plug-n-play base class for creating unique objects that can be stored in a database.
 * Without hard-to-maintain boilerplate code.
 */
public abstract class DynamicTag implements IDynamicTag {

    protected transient final @NotNull MongoImpl mongo;

    protected @Serialize UUID uuid = UUID.randomUUID();

    protected boolean isLoaded = false;

    protected DynamicTag(@NotNull MongoImpl mongo) {
        this.mongo = mongo;
    }

    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    protected boolean load(boolean async, @Nullable LoadLambda lambda) {
        isLoaded = false;

        final SystemManager manager = mongo.getSystemManager();
        final CollectionWrapper collection = mongo.getCollection(getCollection(), Mongo.MongoClientTypes.GLOBAL_DATABASE);

        if (!async) {
            final Document document = collection.raw().find(Filters.eq("uuid", uuid.toString())).first();
            if (document == null) return false; // No document found.

            Documentable.insertDocument(manager, document, this);

            isLoaded = true;
            return true;
        }

        collection.findAsync(Filters.eq("uuid", uuid.toString()), (iterable, exception) -> {
            final Document document = iterable.first();

            boolean isLoaded = true;
            if (exception != null || document == null) isLoaded = false;
            else Documentable.insertDocument(manager, document, this);
            this.isLoaded = isLoaded;

            if (lambda != null) lambda.run(isLoaded);
        });

        return false;
    }

    protected void loadAsync(@Nullable LoadLambda lambda) {
        load(true, lambda);
    }

    protected void loadAsync() {
        loadAsync(null);
    }

    protected boolean loadSync() {
        return load(false, null);
    }

    public boolean save(boolean async, @Nullable SaveLambda lambda, boolean upsert) {
        Preconditions.checkNotNull(mongo, "System (Mongo) cannot be null nor inactive.");

        CollectionWrapper collection = mongo.getCollection(getCollection(), Mongo.MongoClientTypes.GLOBAL_DATABASE);

        if(async)
            collection.replaceOneAsync(
                    Filters.eq("uuid", uuid.toString()), asDocument(),
                    (@Nullable UpdateResult result, @Nullable Exception exception) -> {
                        if(exception != null || result == null) return;

                        if(upsert && result.getModifiedCount() == 0)
                            mongo.getCollection(getCollection(), MongoImpl.MongoClientTypes.GLOBAL_DATABASE)
                                    .insertOneAsync(asDocument(), (@Nullable InsertOneResult result1, @Nullable Exception exception1) -> {
                                        if(result1 == null) return;

                                        if(lambda != null)
                                            lambda.run(result1.getInsertedId() != null);
                                    });
                        else if(lambda != null) lambda.run(result.getModifiedCount() > 0);
                    }
            );
        else {
            boolean did = collection.raw().replaceOne(Filters.eq("uuid", uuid.toString()), asDocument(), new ReplaceOptions().upsert(upsert)).getModifiedCount() >= 1;
        }

        return false;
    }

    @Override
    public boolean save(boolean async, boolean upsert) {
        return save(async, null, upsert);
    }

    public boolean save(boolean async, @Nullable SaveLambda lambda) {
        return save(async, lambda, true);
    }

    @Override
    public boolean save(boolean async) {
        return save(async, null, false);
    }

    @Override
    public boolean save() {
        return save(true, null, false);
    }

    @Override
    public boolean saveSync(boolean upsert) {
        return save(false, null, upsert);
    }

    public boolean saveSync() {
        return saveSync(false);
    }

    public void saveAsync(@Nullable SaveLambda lambda, boolean upsert) {
        save(true, lambda, upsert);
    }

    public void saveAsync(@Nullable SaveLambda lambda) {
        saveAsync(lambda, false);
    }

    public void saveAsync(boolean upsert) {
        saveAsync(null, upsert);
    }

    public void saveAsync() {
        saveAsync(null, false);
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public @NotNull Document asDocument() {
        return Documentable.toDocument(this);
    }

    /**
     * @return The name of the collection that the object is stored in.
     */
    protected String getCollection() {
        return this.getClass().getSimpleName()+"s".toLowerCase(); // Add the 's' to make it plural.
    }

    @Override
    public @DocumentableMethod String serialize() {
        return uuid.toString();
    }

    public interface LoadLambda {
        void run(boolean isLoaded);
    }

}
