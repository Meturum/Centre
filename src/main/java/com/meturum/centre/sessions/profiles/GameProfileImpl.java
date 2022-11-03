package com.meturum.centre.sessions.profiles;

import com.meturum.centra.sessions.GameProfile;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centre.util.mongo.Mongo;
import org.jetbrains.annotations.NotNull;

public class GameProfileImpl extends DynamicTag implements GameProfile {

    private final @NotNull String name;

    public GameProfileImpl(@NotNull String name, @NotNull Mongo mongo) {
        super(mongo);

        this.name = name;
    }

    @Override
    public @NotNull String getNickname() {
        return name;
    }
    @Override
    protected String getCollection() {
        return "profiles";
    }

}
