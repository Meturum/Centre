package com.meturum.centre.util.mongo.tasks;

import com.meturum.centra.mongo.CollectionWrapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;

import javax.annotation.Nullable;

public class InsertOneTask implements Runnable {

    final MongoCollection<Document> collection;
    final Document document;

    @Nullable
    CollectionWrapper.InsertOneLambda lambda = null;

    public InsertOneTask(MongoCollection<Document> collection, Document document, @Nullable CollectionWrapper.InsertOneLambda lambda) {
        this.collection = collection;
        this.document = document;
        this.lambda = lambda;
    }

    public InsertOneTask(MongoCollection<Document> collection, Document document) {
        this.collection = collection;
        this.document = document;
    }

    @Override
    public void run() {
        InsertOneResult response = null;
        Exception exception = null;

        try {
            response = collection.insertOne(document);
        } catch (Exception exception1) {
            exception = exception1;
        } finally {
            if (lambda != null) {
                lambda.run(response, exception);
            }
        }
    }

}
