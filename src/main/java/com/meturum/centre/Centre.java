package com.meturum.centre;

import com.meturum.centra.Centra;
import com.meturum.centra.commands.CommandBuilder;
import com.meturum.centra.system.SystemManager;
import com.meturum.centre.commands.CommandFactoryImpl;
import com.meturum.centre.inventory.InventoryManagerImpl;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.util.SystemFactory;
import com.meturum.centre.util.mongo.MongoImpl;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public final class Centre extends JavaPlugin implements Centra {


    private final SystemFactory manager = new SystemFactory();

    @Override
    public void onEnable() {
        final MongoImpl mongo = manager.register(new MongoImpl(this));
        final SessionFactoryImpl sessionFactory = manager.register(new SessionFactoryImpl(this, mongo));

        final CommandFactoryImpl commandFactory = manager.register(new CommandFactoryImpl(this, sessionFactory));
        manager.register(new InventoryManagerImpl(this, sessionFactory));
        manager.register(new RankFactoryImpl(this, mongo, sessionFactory));

        manager.start();
    }

    @Override
    public void onDisable() {
        manager.stop();
    }

    @Override
    public @NotNull SystemManager getSystemManager() {
        return manager;
    }

    public static void printMessage(@NotNull final String message) {
        getPlugin(Centre.class).getLogger().log(Level.INFO, message);
    }

}
