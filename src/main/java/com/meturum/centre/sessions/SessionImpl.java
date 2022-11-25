package com.meturum.centre.sessions;

import com.meturum.centra.sessions.GameProfile;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.events.ProfileSwitchEvent;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centre.Centre;
import com.meturum.centre.sessions.profiles.GameProfileImpl;
import com.meturum.centre.sessions.ranks.RankImpl;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centre.util.input.SignTextInputImpl;
import com.meturum.centre.util.mongo.MongoImpl;
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

    private transient final Player player;

    private Document settings = new Document();

    @Serialize(method = SerializationMethod.METHOD)
    private RankImpl rank;

    @Serialize(method = SerializationMethod.METHOD, type = GameProfileImpl.class, save = true)
    private GameProfileImpl[] profiles = new GameProfileImpl[0];

    private transient int currentProfile = 0;

    private transient @Nullable SignTextInputImpl textInput;

    public SessionImpl(@NotNull final Player player, @NotNull final MongoImpl mongo, @NotNull final RankImpl defaultRank, @NotNull final Plugin plugin) {
        super(mongo);

        this.uuid = player.getUniqueId();
        this.player = player;
        this.rank = defaultRank;
        this.plugin = plugin;

        addProfile(new GameProfileImpl(GameProfileImpl.CommonNicknames.KIWI, mongo)); // Default Profile...

        load(true, (isLoaded) -> {
            if(!isLoaded) Centre.printMessage("Unable to load session data for, "+getName()+". Using default data...");
            else Centre.printMessage("Successfully loaded session data for, "+getName()+". (MongoDB)");
        });
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
        return rank;
    }

    @Override
    public void setRank(@NotNull final Rank newRank) throws IllegalArgumentException {
        if(this.rank != null) {
            for (String permission : this.rank.getPermissions()) {
                getPlayer().addAttachment(plugin, permission, false);
            }
        }

        this.rank = (RankImpl) newRank;

        getPlayer().setPlayerListName(rank.getFormattedString(this, RankImpl.FormatType.PLAYER_LIST));
        rank.registerToTeam(this);

        for (String permission : newRank.getPermissions()) {
            getPlayer().addAttachment(plugin, permission, true);
        }
    }

    public @NotNull GameProfileImpl[] getProfiles() {
        GameProfileImpl[] profiles = new GameProfileImpl[this.profiles.length];
        System.arraycopy(this.profiles, 0, profiles, 0, this.profiles.length);
        return profiles;
    }

    public @NotNull GameProfileImpl getCurrentProfile() {
        return Objects.requireNonNull(getProfile(currentProfile));
    }

    public void setCurrentProfile(final int index) throws IllegalArgumentException {
        if(getProfile(index) == null)
            throw new IllegalArgumentException("Unable to set profile, a profile at specified index does not exist.");

        ProfileSwitchEvent event = new ProfileSwitchEvent(this, getProfile(index), getProfile(index));
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled()) return;

        currentProfile = index;
    }

    public @Nullable GameProfileImpl getProfile(final int index) {
        try {
            return profiles[index];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public @Nullable GameProfileImpl getProfile(@NotNull final String nickname) {
        for (GameProfileImpl profile : profiles) {
            if(profile.getNickname().equalsIgnoreCase(nickname)) return profile;
        }

        return null;
    }

    public @Nullable GameProfileImpl getProfile(@NotNull final UUID uuid) {
        for (GameProfileImpl profile : profiles) {
            if(profile.getUniqueId().equals(uuid)) return profile;
        }

        return null;
    }

    public boolean addProfile(@NotNull final GameProfile profile) {
        final int PROFILE_MAXIMUM = 7;
        final int PROFILE_MINIMUM = 3;

        int limit = getSettings().containsKey("profile.limit")
                ? getSettings().getInteger("profile.limit")
                : PROFILE_MINIMUM;

        if(profiles.length >= limit || profiles.length >= PROFILE_MAXIMUM) return false;

        // Add profile to array.
        GameProfileImpl[] profiles = new GameProfileImpl[this.profiles.length + 1];
        java.lang.System.arraycopy(profiles, 0, profiles, 0, this.profiles.length);
        profiles[this.profiles.length] = (GameProfileImpl) profile;

        this.profiles = profiles;
        return true;
    }

    public boolean removeProfile(final int index) {
        if(index < 0 || index >= profiles.length) return false; // Index out of bounds.
        if(index == currentProfile) return false; // Cannot remove current profile.

        GameProfileImpl[] profiles = new GameProfileImpl[this.profiles.length - 1];

        for (int i = 0; i < profiles.length; i++) {
            if(i == index) continue;
            profiles[i] = this.profiles[i];
        }

        this.profiles = profiles;
        return true;
    }

    public boolean removeProfile(@NotNull final GameProfile profile) {
        for (int i = 0; i < profiles.length; i++) {
            if(profiles[i].equals(profile)) return removeProfile(i);
        }

        return false;
    }

    public void sendMessage(@NotNull final String message) {
        getPlayer().sendMessage(message);
    }

    public @Nullable SignTextInputImpl getTextInput() {
        return textInput;
    }

    public void setTextInput(@Nullable final SignTextInputImpl textInput) {
        this.textInput = textInput;
    }

    public @Nullable SignTextInputImpl createTextInput() {
        return new SignTextInputImpl(this); // Create a new text input.
    }

    public void kick(@NotNull final String reason) {
        getPlayer().kickPlayer(reason);
    }

    @Override
    protected String getCollection() {
        return "users";
    }

}
