package com.meturum.centre.util.mongo.tasks;

import com.meturum.centra.mongo.CollectionWrapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

public class ReplaceOneTask implements Runnable {

    final MongoCollection<Document> collection;
    final Bson filter;
    final Document document;
    ReplaceOptions options;

    @Nullable
    CollectionWrapper.ReplaceOneLambda lambda = null;

    public ReplaceOneTask(MongoCollection<Document> collection, Bson filter, Document d, ReplaceOptions options, @Nullable CollectionWrapper.ReplaceOneLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.document = d;
        this.options = options;
        this.lambda = lambda;
    }

    public ReplaceOneTask(MongoCollection<Document> collection, Bson filter, Document d, @Nullable ReplaceOptions options) {
        this.collection = collection;
        this.filter = filter;
        this.document = d;
        this.options = options;
    }

    public ReplaceOneTask(MongoCollection<Document> collection, Bson filter, Document d, @Nullable CollectionWrapper.ReplaceOneLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.document = d;
        this.lambda = lambda;
    }

    public ReplaceOneTask(MongoCollection<Document> collection, Bson filter, Document d) {
        this.collection = collection;
        this.filter = filter;
        this.document = d;
    }

    @Override
    public void run() {
        UpdateResult response = null;
        Exception exception = null;

        try {
            if (options != null) {
                response = collection.replaceOne(filter, document, options);
            } else response = collection.replaceOne(filter, document);
        } catch (Exception exception1) {
            exception = exception1;
        } finally {
            if (lambda != null) {
                lambda.run(response, exception);
            }
        }
    }

}
