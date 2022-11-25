package com.meturum.centre.util.mongo.tasks;

import com.meturum.centra.mongo.CollectionWrapper;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FindTask implements Runnable {

    final MongoCollection<Document> collection;
    Bson filter;

    @Nullable
    CollectionWrapper.FindLambda lambda;

    public FindTask(MongoCollection<Document> collection, Bson filter, @NotNull CollectionWrapper.FindLambda lambda) {
        this.collection = collection;
        this.filter = filter;
        this.lambda = lambda;
    }

    public FindTask(MongoCollection<Document> collection, @Nullable CollectionWrapper.FindLambda lambda) {
        this.collection = collection;
        this.lambda = lambda;
    }

    @Override
    public void run() {
        FindIterable<Document> response = null;
        Exception exception = null;

        try {
            if (filter != null) {
                response = collection.find(filter);
            } else response = collection.find();
        } catch (Exception e) {
            exception = e;
        } finally {
            if (lambda != null) {
                lambda.run(response, exception);
            }
        }
    }

}
