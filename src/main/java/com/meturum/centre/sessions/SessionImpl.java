package com.meturum.centre.sessions;

import com.meturum.centra.sessions.GameProfile;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.events.ProfileSwitchEvent;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.sessions.ranks.RankImpl;
import com.meturum.centra.conversions.Documentable;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centre.util.mongo.Mongo;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

public class SessionImpl extends DynamicTag implements Session {

    private transient final Plugin plugin;
    private transient final RankFactoryImpl rankFactory;

    private transient final Player player;

    private Document settings = new Document();

    private @Serialize(method = SerializationMethod.REFERENCE) RankImpl rank;

    private transient GameProfile[] profiles = new GameProfile[0];

    private transient int profile = 0;

    public SessionImpl(@NotNull Player player, @NotNull Mongo mongo, @NotNull RankFactoryImpl rankFactory, @NotNull Plugin plugin) {
        super(mongo);

        this.player = player;
        this.rankFactory = rankFactory;
        this.plugin = plugin;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull String getName() {
        return player.getName();
    }

    public @NotNull Document getSettings() {
        return settings;
    }

    @Override
    public @NotNull Rank getRank() {
        if(rank == null) setRank(null);
        return rank;
    }

    @Override
    public void setRank(@Nullable Rank rank_wrapper) throws IllegalArgumentException{
        if(rank_wrapper == null) {
            rank_wrapper = rankFactory.search();
        }

        if(!(rank_wrapper instanceof RankImpl newRank))
            throw new IllegalArgumentException("Rank must be an instance of eRank.");

        if(this.rank != null) {
            for (String permission : this.rank.getPermissions()) {
                getPlayer().addAttachment(plugin, permission, false);
            }
        }

        this.rank = newRank;

        getPlayer().setPlayerListName(newRank.getFormattedString(this, RankImpl.FormatType.PLAYER_LIST));
        newRank.registerToTeam(this);

        for (String permission : newRank.getPermissions()) {
            getPlayer().addAttachment(plugin, permission, true);
        }
    }

    public @NotNull GameProfile[] getProfiles() {
        GameProfile[] profiles = new GameProfile[this.profiles.length];
        System.arraycopy(this.profiles, 0, profiles, 0, this.profiles.length);
        return profiles;
    }

    public @NotNull GameProfile getCurrentProfile() {
        return Objects.requireNonNull(getProfile(profile));
    }

    public void setCurrentProfile(int index) throws IllegalArgumentException {
        if(getProfile(index) == null)
            throw new IllegalArgumentException("Unable to set profile, a profile at specified index does not exist.");

        ProfileSwitchEvent event = new ProfileSwitchEvent(this, getProfile(index), getProfile(index));
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled()) return;

        profile = index;
    }

    public @Nullable GameProfile getProfile(int index) {
        try {
            return profiles[index];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public @Nullable GameProfile getProfile(@NotNull String nickname) {
        for (GameProfile profile : profiles) {
            if(profile.getNickname().equalsIgnoreCase(nickname)) return profile;
        }

        return null;
    }

    public @Nullable GameProfile getProfile(@NotNull UUID uuid) {
        for (GameProfile profile : profiles) {
            if(profile.getUniqueId().equals(uuid)) return profile;
        }

        return null;
    }

    public boolean addProfile(@NotNull GameProfile profile) {
        final int PROFILE_MAXIMUM = 7;
        final int PROFILE_MINIMUM = 3;

        int limit = getSettings().containsKey("profile.limit")
                ? getSettings().getInteger("profile.limit")
                : PROFILE_MINIMUM;

        if(profiles.length >= limit || profiles.length >= PROFILE_MAXIMUM) return false;

        // Add profile to array.
        GameProfile[] profiles = new GameProfile[this.profiles.length + 1];
        java.lang.System.arraycopy(profiles, 0, profiles, 0, this.profiles.length);
        profiles[this.profiles.length] = profile;

        this.profiles = profiles;
        return true;
    }

    public boolean removeProfile(int index) {
        if(index < 0 || index >= profiles.length) return false; // Index out of bounds.
        if(index == profile) return false; // Cannot remove current profile.

        GameProfile[] profiles = new GameProfile[this.profiles.length - 1];

        for (int i = 0; i < profiles.length; i++) {
            if(i == index) continue;
            profiles[i] = this.profiles[i];
        }

        this.profiles = profiles;
        return true;
    }

    public boolean removeProfile(@NotNull GameProfile profile) {
        for (int i = 0; i < profiles.length; i++) {
            if(profiles[i].equals(profile)) return removeProfile(i);
        }

        return false;
    }

    public void sendMessage(@NotNull String message) {
        getPlayer().sendMessage(message);
    }

    public void kick(@NotNull String reason) {
        getPlayer().kickPlayer(reason);
    }

    public static SessionImpl of(@NotNull Player player, @NotNull Document document, @NotNull Mongo mongo, @NotNull RankFactoryImpl rankFactory, @NotNull Plugin plugin) {
        SessionImpl session = new SessionImpl(player, mongo, rankFactory, plugin);
        Documentable.insertDocument(mongo.getSystemManager(), document, session);

        return session;
    }

}
