package com.meturum.centre.sessions.ranks;

import com.google.common.base.Preconditions;
import com.meturum.centra.mongo.IMongo;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.sessions.SessionImpl;
import com.meturum.centra.ColorList;
import com.meturum.centra.conversions.Documentable;
import com.meturum.centre.util.DynamicTag;
import com.meturum.centra.conversions.annotations.DocumentableMethod;
import com.meturum.centre.util.mongo.Mongo;
import com.mongodb.client.model.Filters;
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

public class RankImpl extends DynamicTag implements Rank {

    private String name;
    private @Serialize(method = SerializationMethod.STRING) ColorList color;
    private List<String> permissions;
    private @NotNull String icon = "";

    private final Document properties = new Document();

    protected RankImpl(@Nullable Mongo mongo) {
        super(mongo);
    }

    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name.toLowerCase();
    }

    public @NotNull ColorList getColor() {
        return color;
    }

    public void setColor(@NotNull ColorList color) {
        this.color = color;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void addPermission(@NotNull String permission) {
        permissions.add(permission);
    }

    public void removePermission(@NotNull String permission) {
        permissions.remove(permission);
    }

    public void setPermissions(@NotNull List<String> permissions) {
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

    public void setIcon(@NotNull String icon) {
        this.icon = icon;
        patchIcon();
    }

    public <T> T getProperty(@NotNull String key) {
        return (T) properties.get(key);
    }

    public void setProperty(@NotNull String key, @NotNull Object value) {
        properties.put(key, value);
    }

    public void registerToTeam(@NotNull SessionImpl session) {
        if(!session.getRank().equals(this)) return;

        if(!getTeam().hasEntry(session.getName()))
            getTeam().addEntry(session.getName());
    }

    private Team getTeam() {
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

    public @Nullable String getFormattedString(Session session, FormatType type){
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
    private static Rank _fromReference(@NotNull SystemManager manager, @NotNull UUID value) {
        RankFactoryImpl factory = manager.search(RankFactoryImpl.class);
        Preconditions.checkArgument(factory != null, "System (RankFactory) cannot be null nor inactive.");

        return factory.search(value);
    }

    public static RankImpl of(@NotNull Mongo mongo, @NotNull String name) {
        Preconditions.checkNotNull(mongo, "System (Mongo) cannot be null nor inactive.");

        Document document = mongo.getCollection("ranks", IMongo.MongoClientTypes.GLOBAL_DATABASE).raw().find(Filters.eq("name", name.toLowerCase())).first();
        if(document == null) return null;

        RankImpl rank = new RankImpl(mongo);
        Documentable.insertDocument(mongo.getSystemManager(), document, rank);

        return rank;
    }

    public static RankImpl of(@NotNull Mongo mongo, @NotNull Document document) throws Exception {
        return Documentable.fromDocument(mongo.getSystemManager(), document, RankImpl.class);
    }

    public enum FormatType {
        CHAT, DISPLAY_NAME, PLAYER_LIST
    }

}
