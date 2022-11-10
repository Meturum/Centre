package com.meturum.centre.sessions.ranks;

import com.meturum.centra.mongo.Mongo;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.SessionFactory;
import com.meturum.centra.sessions.ranks.Rank;
import com.meturum.centra.conversions.Documentable;
import com.meturum.centra.sessions.ranks.RankFactory;
import com.meturum.centre.Centre;
import com.meturum.centre.util.EmojiList;
import com.meturum.centre.util.SystemImpl;
import com.meturum.centre.util.mongo.MongoImpl;
import com.mongodb.client.FindIterable;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankFactoryImpl extends SystemImpl implements RankFactory {

    private final MongoImpl mongo;
    private final SessionFactory sessionFactory;

    private final List<RankImpl> ranks = new ArrayList<>();

    public RankFactoryImpl(@NotNull Centre centre, @NotNull MongoImpl mongo, @NotNull SessionFactory sessionFactory) {
        super(centre);

        this.mongo = mongo;
        this.sessionFactory = sessionFactory;

        mongo.getCollection("ranks", Mongo.MongoClientTypes.GLOBAL_DATABASE).findAsync(
                (@Nullable FindIterable<Document> iterable, @Nullable Exception exception) -> {
                    RankFactoryImpl factory = centre.getSystemManager().search(RankFactoryImpl.class);

                    for (Document document : iterable) {
                        try {
                            RankImpl rank = Documentable.fromDocument(centre.getSystemManager(), document, RankImpl.class);
                            factory.register(rank);
                        }catch (Exception ignored) { ignored.printStackTrace(); }
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

        Document document = mongo.getCollection("ranks", Mongo.MongoClientTypes.GLOBAL_DATABASE).raw().find(Filters.eq("uuid", uuid.toString())).first();
        return searchDeep(document);
    }

    public @Nullable Rank search(@NotNull UUID uuid) {
        return search(uuid, false);
    }

    public @Nullable Rank search(@NotNull String name, boolean deep) {
        if(!deep)
            return ranks.stream().filter(rank -> rank.getName().equalsIgnoreCase(name)).findFirst().orElse(null);

        Document document = mongo.getCollection("ranks", Mongo.MongoClientTypes.GLOBAL_DATABASE).raw().find(Filters.eq("name", name)).first();

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

    @EventHandler
    public void onAPCE(AsyncPlayerChatEvent event) {
        Session session = sessionFactory.search(event.getPlayer());
        if(session == null) return;

        Rank rank = session.getRank();
        if(rank == null) return;

        if(session.getPlayer().isOp() || session.getSettings().containsKey("chat.convert-colors") && session.getSettings().getBoolean("chat.convert-colors"))
            event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

        if(session.getPlayer().isOp() || session.getSettings().containsKey("chat.convert-emojis") && session.getSettings().getBoolean("chat.convert-emojis"))
            event.setMessage(EmojiList.read(event.getMessage()));

        if(rank instanceof RankImpl)
            event.setFormat(((RankImpl) rank).getFormattedString(session, RankImpl.FormatType.CHAT) + event.getMessage());
    }

}
