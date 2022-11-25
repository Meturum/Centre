package com.meturum.centre.sessions.ranks;

import com.google.common.base.Preconditions;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.sessions.SessionImpl;
import com.meturum.centra.ColorList;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centra.conversions.annotations.DocumentableMethod;
import com.meturum.centre.util.mongo.MongoImpl;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import javax.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public final class RankImpl extends DynamicTag implements Rank {

    private String name;
    private @Serialize(method = SerializationMethod.METHOD) ColorList color;
    private List<String> permissions;
    private @NotNull String icon = "";

    private final Document properties = new Document();

    @DocumentableMethod
    public RankImpl(@Nullable final MongoImpl mongo) {
        super(mongo);
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull final String name) {
        this.name = name.toLowerCase();
    }

    public @NotNull ColorList getColor() {
        return color;
    }

    public void setColor(@NotNull final ColorList color) {
        this.color = color;
    }

    public @NotNull List<String> getPermissions() {
        return permissions;
    }

    public void addPermission(@NotNull final String permission) {
        permissions.add(permission);
    }

    public void removePermission(@NotNull final String permission) {
        permissions.remove(permission);
    }

    public void setPermissions(@NotNull final List<String> permissions) {
        this.permissions = permissions;
    }

    public @NotNull String getIcon() {
        return icon;
    }

    public boolean hasIcon() {
        return !StringUtils.isEmpty(icon);
    }

    private void patchIcon() {
        getTeam().setPrefix(hasIcon() ? ChatColor.WHITE + getIcon() + "\uF823" : "" + ChatColor.RESET);
    }

    public void setIcon(@NotNull final String icon) {
        this.icon = icon;
        patchIcon();
    }

    public <T> T getProperty(@NotNull final String key) {
        return (T) properties.get(key);
    }

    public void setProperty(@NotNull final String key, @NotNull final Object value) {
        properties.put(key, value);
    }

    public void registerToTeam(@NotNull final SessionImpl session) {
        if(!session.getRank().equals(this)) return;

        Team team = getScoreboard().getEntryTeam(session.getName()); // Get the team the player is in.
        if(team != null) team.removeEntry(session.getName()); // Remove the player from the team.

        getTeam().addEntry(session.getName()); // Add the player to the team.
    }

    private @NotNull Team getTeam() {
        Team team = getScoreboard().getTeam(getName());

        if (team == null) {
            team = getScoreboard().registerNewTeam(getName());
            patchIcon();
        }

        return team;
    }

    private @NotNull Scoreboard getScoreboard() {
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public @Nullable String getFormattedString(@NotNull final Session session, @NotNull final FormatType type){
        return switch (type) {
            case CHAT -> ChatColor.WHITE + (hasIcon() ? getIcon() : getColor() + "" + ChatColor.BOLD +getName().toUpperCase() + ChatColor.RESET) + "\uF823" + getColor() + session.getName() + ChatColor.RESET + ": " + Rank.brighten(getColor().v.getColor());
            case DISPLAY_NAME -> ChatColor.WHITE + (hasIcon() ? getIcon() : getColor() + "" + ChatColor.BOLD +getName().toUpperCase() + ChatColor.RESET) + getColor() + session.getName() + ChatColor.RESET;
            case PLAYER_LIST -> ChatColor.WHITE + (hasIcon() ? getIcon() + "\uF823" : "") + getColor() + session.getName() + ChatColor.RESET;
        };
    }

    /**
     * @return The unique identifier of this object.
     */
    @Override
    public String toString() {
        return uuid.toString();
    }

    @DocumentableMethod
    private static Rank deserialize(@NotNull final SystemManager manager, @NotNull final Object value) {
        RankFactoryImpl factory = manager.search(RankFactoryImpl.class);
        Preconditions.checkArgument(factory != null, "System (RankFactory) cannot be null nor inactive.");

        return factory.search(UUID.fromString((String) value));
    }

    public enum FormatType {
        CHAT, DISPLAY_NAME, PLAYER_LIST
    }

}
