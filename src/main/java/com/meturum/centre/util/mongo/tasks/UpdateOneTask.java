package com.meturum.centre.util.mongo.tasks;

import com.meturum.centra.mongo.CollectionWrapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

public class UpdateOneTask implements Runnable {

    final MongoCollection<Document> collection;
    final Bson filter;
    final Bson document;
    UpdateOptions options;

    @Nullable
    CollectionWrapper.UpdateOneLambda lambda = null;

    public UpdateOneTask(MongoCollection<Document> collection, Bson filter, Bson document, UpdateOptions options, @Nullable CollectionWrapper.UpdateOneLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.document = document;
        this.options = options;
        this.lambda = lambda;
    }

    public UpdateOneTask(MongoCollection<Document> collection, Bson filter, Bson document, @Nullable CollectionWrapper.UpdateOneLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.document = document;
        this.lambda = lambda;
    }

    public UpdateOneTask(MongoCollection<Document> collection, Bson filter, Bson document) {
        this.collection = collection;
        this.filter = filter;
        this.document = document;
    }

    @Override
    public void run() {
        UpdateResult response = null;
        Exception exception = null;

        try {
            if (options != null) {
                response = collection.updateOne(filter, document, options);
            } else response = collection.updateOne(filter, document);
        } catch (Exception exception1) {
            exception = exception1;
        } finally {
            if (lambda != null) {
                lambda.run(response, exception);
            }
        }
    }

}
