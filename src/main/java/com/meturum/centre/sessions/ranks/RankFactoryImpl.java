package com.meturum.centre.sessions.ranks;

import com.meturum.centra.Centra;
import com.meturum.centra.mongo.IMongo;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centra.conversions.Documentable;
import com.meturum.centra.sessions.ranks.RankFactory;
import com.meturum.centre.Centre;
import com.meturum.centre.util.SystemImpl;
import com.meturum.centre.util.mongo.Mongo;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankFactoryImpl extends SystemImpl implements RankFactory {

    private final Mongo mongo;

    private final List<RankImpl> ranks = new ArrayList<>();

    public RankFactoryImpl(@NotNull Centre centre, @NotNull Mongo mongo) {
        super(centre);

        this.mongo = mongo;

        mongo.getCollection("ranks", IMongo.MongoClientTypes.GLOBAL_DATABASE).findAsync(
                (@Nullable FindIterable<Document> iterable, @Nullable Exception exception) -> {
                    RankFactoryImpl factory = centre.getSystemManager().search(RankFactoryImpl.class);

                    for (Document document : iterable) {
                        try {
                            RankImpl rank = Documentable.fromDocument(centre.getSystemManager(), document, RankImpl.class);
                            factory.register(rank);
                        }catch (Exception ignored) { }
                    }
                }
        );
    }

    public boolean register(@NotNull RankImpl rank) {
        if(contains(rank.getName()) || contains(rank.getUniqueId()))
            return false;

        ranks.add(rank);

        return true;
    }

    public @Nullable Rank search(@NotNull UUID uuid, boolean deep) {
        if(!deep)
            return ranks.stream().filter(rank -> rank.getUniqueId().equals(uuid)).findFirst().orElse(null);

        Document document = mongo.getCollection("ranks", IMongo.MongoClientTypes.GLOBAL_DATABASE).raw().find(Filters.eq("uuid", uuid.toString())).first();
        return searchDeep(document);
    }

    public @Nullable Rank search(@NotNull UUID uuid) {
        return search(uuid, false);
    }

    public @Nullable Rank search(@NotNull String name, boolean deep) {
        if(!deep)
            return ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        Document document = mongo.getCollection("ranks", IMongo.MongoClientTypes.GLOBAL_DATABASE).raw().find(Filters.eq("name", name)).first();

        return searchDeep(document);
    }

    private Rank searchDeep(Document document) {
        if(document == null) return null;

        RankImpl rank = null;
        try {
            rank = Documentable.fromDocument(centre.getSystemManager(), document, RankImpl.class);
            if(rank == null) return null;

            register(rank);
        }catch (Exception ignored) { }

        return rank;
    }

    public @Nullable Rank search(@NotNull String name) {
        return search(name, false);
    }

    public @Nullable Rank search() {
        return null;
    }

    public boolean contains(@NotNull UUID uuid, boolean deep) {
        return false;
    }

    public boolean contains(@NotNull UUID uuid) {
        return contains(uuid, false);
    }

    public boolean contains(@NotNull String name, boolean deep) {
        return false;
    }

    public boolean contains(@NotNull String name) {
        return contains(name, false);
    }

    public boolean unregister(@NotNull RankImpl rank) {
        return false;
    }

}
