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

public final class SessionFactoryImpl extends SystemImpl implements SessionFactory {

    private final MongoImpl mongo;

    private final List<SessionImpl> sessionsList = new ArrayList<>();

    public SessionFactoryImpl(@NotNull final Centre centre, @NotNull final MongoImpl mongo) {
        super(centre);

        this.mongo = mongo;
    }

    @Override
    protected void _stop() {
        for(SessionImpl session : List.copyOf(sessionsList)) {
            close(session, false);
        }
    }

    public @NotNull SessionImpl open(@NotNull final Player player) {
        if(contains(player))
            return search(player);

        SystemManager manager = centre.getSystemManager();
        SessionImpl session = new SessionImpl(player, mongo, manager.search(RankFactoryImpl.class).getDefaultRank(), centre);
        sessionsList.add(session);
        return session;
    }

    public @Nullable SessionImpl search(@NotNull final UUID uuid) {
        for (SessionImpl session : sessionsList) {
            if(session.getPlayer().getUniqueId().equals(uuid))
                return session;
        }

        return null;
    }

    public @Nullable SessionImpl search(@NotNull final Player player) {
        return search(player.getUniqueId());
    }

    public boolean contains(@NotNull final UUID uuid) {
        return search(uuid) != null;
    }

    public boolean contains(@NotNull final Player player) {
        return contains(player.getUniqueId());
    }

    public boolean contains(@NotNull final Session session) {
        return contains(session.getPlayer().getUniqueId());
    }

    public boolean close(@NotNull final Session session, final boolean async) {
        if(!contains(session))
            return false;

        if(async) session.saveAsync(true); else session.saveSync(true);
        if(session.getPlayer().isOnline())
            session.getPlayer().kickPlayer(ColorList.RED+"Unable to verify your session. Please rejoin the server. (Error: SFI-cSa)");

        sessionsList.remove((SessionImpl) session);

        return true;
    }

    public boolean close(@NotNull final Session session) {
        return close(session, true);
    }

    @EventHandler
    public void onPJE(@NotNull final PlayerJoinEvent event) {
        open(event.getPlayer());
        event.setJoinMessage("");
    }

    @EventHandler
    public void onPQE(@NotNull final PlayerQuitEvent event) {
        close(search(event.getPlayer()));
        event.setQuitMessage("");
    }

}
