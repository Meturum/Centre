package com.meturum.centre;

import com.meturum.centra.Centra;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.util.SystemFactory;
import com.meturum.centre.util.mongo.Mongo;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Centre extends JavaPlugin implements Centra {

    private boolean enabled = false;

    private final SystemFactory manager = new SystemFactory();

    @Override
    public void onEnable() {
        if (enabled) return;

        manager.registerAll(
                new Mongo(this),

                new SessionFactoryImpl(this, manager.search(Mongo.class)),
                new RankFactoryImpl(this, manager.search(Mongo.class))
        );

        enabled = true;
    }

    @Override
    public @NotNull SystemManager getSystemManager() {
        return manager;
    }

}
