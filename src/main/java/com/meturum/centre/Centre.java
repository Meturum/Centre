package com.meturum.centre;

import com.meturum.centra.Centra;
import com.meturum.centra.sessions.SessionFactory;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.commands.CommandFactory;
import com.meturum.centre.interfaces.InterfaceManager;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.util.SystemFactory;
import com.meturum.centre.util.mongo.MongoImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Centre extends JavaPlugin implements Centra {

    private boolean enabled = false;

    private final SystemFactory manager = new SystemFactory();

    @Override
    public void onEnable() {
        if (enabled) return;

        MongoImpl mongo = manager.register(new MongoImpl(this));
        manager.register(new CommandFactory(this));
        SessionFactory sessionFactory = manager.register(new SessionFactoryImpl(this, mongo));
        manager.register(new InterfaceManager(this, sessionFactory));
        manager.register(new RankFactoryImpl(this, mongo, sessionFactory));

        enabled = true;
    }

    @Override
    public @NotNull SystemManager getSystemManager() {
        return manager;
    }

}
