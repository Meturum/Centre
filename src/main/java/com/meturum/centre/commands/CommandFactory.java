package com.meturum.centre.commands;

import com.meturum.centra.ColorList;
import com.meturum.centra.interfaces.Interface;
import com.meturum.centra.interfaces.container.Container;
import com.meturum.centra.interfaces.item.ItemWrapper;
import com.meturum.centre.Centre;
import com.meturum.centre.interfaces.ContainerImpl;
import com.meturum.centre.interfaces.InterfaceImpl;
import com.meturum.centre.interfaces.InterfaceManager;
import com.meturum.centre.util.EmojiList;
import com.meturum.centre.util.SystemImpl;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

public final class CommandFactory extends SystemImpl {

    public CommandFactory(@NotNull Centre centre) {
        super(centre);

        registerCommand(new CommandTree("centre")
                .executes((context) -> {
                    if (centre == null) return;
                    String message = EmojiList.SUCCESS_ICON.toString() + ColorList.GRAY + " Centre is currently running version " + ColorList.GREEN + centre.getDescription().getVersion() + ColorList.GRAY + ".";

                    context.getSender().sendMessage(message);
                })
        );

        registerCommand(new CommandTree("debug")
                .executes((context) -> {
                    InterfaceImpl ui = new InterfaceImpl(3, getSystemManager().search(InterfaceManager.class))
                            .setTitle("Debug Interface")
                            .setOverlay("");

                    Container container = ui.createContainer(0, 0, 3);
                    container.addItem(new ItemWrapper(Material.APPLE));

                    ui.view((Player) context.getSender());
                })
        );
    }

    /**
     * Registers a command to the server. If the command already exists, it will be overwritten.
     *
     * @param command The command to register.
     * @return true if the command has been registered, false otherwise, which indicates the fallbackPrefix, "centro", was used one or more times for that command
     */
    public boolean registerCommand(@NotNull CommandTree command) throws RuntimeException {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

            return commandMap.register("centre", command.asBukkitCommand());
        } catch (Exception e) { /* FIXME: Error Reporting */ }

        return false;
    }

}
