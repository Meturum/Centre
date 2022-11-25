package com.meturum.centre.sessions.profiles;

import com.meturum.centra.conversions.annotations.DocumentableMethod;
import com.meturum.centra.mongo.Mongo;
import com.meturum.centra.sessions.GameProfile;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centre.util.mongo.MongoImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class GameProfileImpl extends DynamicTag implements GameProfile {

    private final @NotNull String nickname;

    @DocumentableMethod
    public GameProfileImpl(@Nullable final String name, @NotNull final MongoImpl mongo) {
        super(mongo);

        this.nickname = name != null ? name : "";
    }

    public GameProfileImpl(@NotNull final CommonNicknames nickname, @NotNull final MongoImpl mongo) {
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

    public static GameProfileImpl of(@NotNull final UUID uuid, @NotNull final MongoImpl mongo) {
        GameProfileImpl profile = new GameProfileImpl("", mongo);
        profile.uuid = uuid;

        profile.load(false, null);
        return profile;
    }

    @DocumentableMethod
    public static @NotNull GameProfileImpl deserialize(@NotNull final SystemManager manager, @NotNull final Object uuid) {
        return GameProfileImpl.of(UUID.fromString((String) uuid), manager.search(MongoImpl.class));
    }

    public enum CommonNicknames {
        APPLE, ORANGE, BANANA, KIWI
    }

}
