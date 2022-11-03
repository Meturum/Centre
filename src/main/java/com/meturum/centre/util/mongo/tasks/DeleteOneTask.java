package com.meturum.centre.util.mongo.tasks;

import com.meturum.centra.mongo.CollectionWrapper;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Nullable;

public class DeleteOneTask implements Runnable {

    final MongoCollection<Document> collection;
    final Bson filter;

    @Nullable
    CollectionWrapper.DeleteOneLambda lambda;

    public DeleteOneTask(MongoCollection<Document> collection, Bson filter, @Nullable CollectionWrapper.DeleteOneLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.lambda = lambda;
    }

    public DeleteOneTask(MongoCollection<Document> collection, Bson filter) {
        this.collection = collection;
        this.filter = filter;
    }

    @Override
    public void run() {
        DeleteResult response = null;
        Exception exception = null;

        try {
            if (lambda != null) {
                response = collection.deleteOne(filter);
            } else collection.deleteOne(filter);
        } catch (Exception exception1) {
            exception = exception1;
        } finally {
            if (lambda != null) {
                lambda.run(response, exception);
            }
        }
    }

}