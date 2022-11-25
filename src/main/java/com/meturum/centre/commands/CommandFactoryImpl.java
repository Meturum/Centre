package com.meturum.centre.commands;

import com.meturum.centra.ColorList;
import com.meturum.centra.commands.CommandBuilder;
import com.meturum.centra.commands.CommandFactory;
import com.meturum.centre.Centre;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.SessionImpl;
import com.meturum.centra.EmojiList;
import com.meturum.centre.util.SystemImpl;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class CommandFactoryImpl extends SystemImpl implements CommandFactory {

    private final SessionFactoryImpl sessionFactory;

    public CommandFactoryImpl(@NotNull final Centre centre, @NotNull final SessionFactoryImpl sessionFactory) {
        super(centre);

        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void _init() {
        registerCommand(new CommandBuilder("centre")
                .executes((context) -> {
                    if (centre == null) return;
                    String message = EmojiList.SUCCESS_ICON.toString() + ColorList.GRAY + " Centre is currently running version " + ColorList.GREEN + centre.getDescription().getVersion() + ColorList.GRAY + ".";

                    context.getSender().sendMessage(message);
                })
        );

        registerCommand(new CommandBuilder("debug")
                .executes((context) -> {
                    SessionImpl session = (SessionImpl) context.getSession();
                    Player bukkitPlayer = session.getPlayer();
                    ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

                    session.sendMessage(nmsPlayer.getAdvancements().advancements.size()+" advancements currently loaded");
                })
        );
    }

    /**
     * Registers a command to the server. If the command already exists, it will be overwritten.
     *
     * @param command The command to register.
     * @return true if the command has been registered, false otherwise, which indicates the fallbackPrefix, "centro", was used one or more times for that command
     */
    public boolean registerCommand(@NotNull final CommandBuilder command)  {
        CraftServer server = (CraftServer) Bukkit.getServer();

        CommandTree commandTree = new CommandTree(command);
        server.getCommandMap().register("centre", commandTree.asBukkitCommand(sessionFactory));

        return false;
    }

}
