package com.meturum.centre.sessions;

import com.meturum.centra.ColorList;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.SessionFactory;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.Centre;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.util.SystemImpl;
import com.meturum.centre.util.mongo.MongoImpl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SessionFactoryImpl extends SystemImpl implements SessionFactory {

    private final MongoImpl mongo;

    private final List<?> sessions = new ArrayList<>();

    public SessionFactoryImpl(@NotNull Centre centre, @NotNull MongoImpl mongo) {
        super(centre);

        this.mongo = mongo;
    }

    private final List<Session> sessionsList = new ArrayList<>();

    public @NotNull Session open(@NotNull Player player) {
        if(contains(player))
            return search(player);

        SystemManager manager = centre.getSystemManager();
        Session session = new SessionImpl(player, mongo, manager.search(RankFactoryImpl.class), centre);
        sessionsList.add(session);
        return session;
    }

    public @Nullable Session search(@NotNull UUID uuid) {
        for (Session session : sessionsList) {
            if(session.getPlayer().getUniqueId().equals(uuid))
                return session;
        }

        return null;
    }

    public @Nullable Session search(@NotNull Player player) {
        return search(player.getUniqueId());
    }

    public boolean contains(@NotNull UUID uuid) {
        return search(uuid) != null;
    }

    public boolean contains(@NotNull Player player) {
        return contains(player.getUniqueId());
    }

    public boolean contains(@NotNull Session session) {
        return contains(session.getPlayer().getUniqueId());
    }

    public boolean close(@NotNull Session session, boolean async) {
        if(!contains(session))
            return false;

        if(async) ((SessionImpl) session).saveAsync(); else ((SessionImpl) session).saveSync();
        if(session.getPlayer().isOnline())
            session.getPlayer().kickPlayer(ColorList.RED+"Unable to verify your session. Please rejoin the server. (Error: SFI-cSa)");

        sessionsList.remove(session);

        return true;
    }

    public boolean close(@NotNull Session session) {
        return close(session, true);
    }

    @EventHandler
    public void onPJE(PlayerJoinEvent event) {
        open(event.getPlayer());
        event.setJoinMessage("");
    }

    @EventHandler
    public void onPQE(PlayerQuitEvent event) {
        close(search(event.getPlayer()));
        event.setQuitMessage("");
    }

}
