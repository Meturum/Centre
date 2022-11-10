package com.meturum.centre.sessions.profiles;

import com.meturum.centra.conversions.Documentable;
import com.meturum.centra.mongo.Mongo;
import com.meturum.centra.sessions.GameProfile;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centre.util.mongo.MongoImpl;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GameProfileImpl extends DynamicTag implements GameProfile {

    private final @NotNull String nickname;

    public GameProfileImpl(@NotNull String name, @NotNull Mongo mongo) {
        super(mongo);

        this.nickname = name;
    }

    public GameProfileImpl(@NotNull CommonNicknames nickname, @NotNull Mongo mongo) {
        this(nickname.name(), mongo);
    }

    @Override
    public @NotNull String getNickname() {
        return nickname;
    }
    @Override
    protected String getCollection() {
        return "profiles";
    }

    public static GameProfileImpl of(@NotNull UUID uuid, @NotNull Mongo mongo) {
        GameProfileImpl profile = new GameProfileImpl("", mongo);
        profile.uuid = uuid;

        FindIterable<Document> iterable = mongo.getCollection(profile.getCollection(), Mongo.MongoClientTypes.GLOBAL_DATABASE).raw()
                .find(Filters.eq("uuid", uuid.toString()));

        Document document = iterable.first();
        if(document == null) return null;

        Documentable.insertDocument(((MongoImpl) mongo).getSystemManager(), document, profile);

        return profile;
    }

    public enum CommonNicknames {
        APPLE, ORANGE, BANANA, KIWI
    }

}
