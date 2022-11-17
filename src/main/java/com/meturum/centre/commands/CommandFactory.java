package com.meturum.centre.commands;

import com.meturum.centra.ColorList;
import com.meturum.centra.inventory.CustomInventory;
import com.meturum.centra.inventory.InventoryManager;
import com.meturum.centra.inventory.actions.ActionEventContext;
import com.meturum.centra.inventory.actions.GeneralAction;
import com.meturum.centra.inventory.container.Container;
import com.meturum.centra.inventory.item.ItemBuilder;
import com.meturum.centra.inventory.item.Position;
import com.meturum.centre.Centre;
import com.meturum.centre.sessions.ranks.RankFactoryImpl;
import com.meturum.centre.sessions.ranks.RankImpl;
import com.meturum.centre.util.EmojiList;
import com.meturum.centre.util.SystemImpl;
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
                    InventoryManager manager = getSystemManager().search(InventoryManager.class);
                    RankFactoryImpl rankFactory = getSystemManager().search(RankFactoryImpl.class);

                    final CustomInventory inventory = manager.createInventory(3)
                            .setMaximized()
                            .setTitle("Debug UI");

                    final Container container = inventory.createContainer(0, 0, 3, 3)
                            .setAllowedActions(GeneralAction.PICKUP)
                            .setItem(new ItemBuilder(Material.DIAMOND)
                                            .setCustomName(ColorList.RED+"Debug Button")
                                    , new Position(2, 2))
                            .interacts((action) -> {
                                ItemBuilder item = action.getCurrentItem();
                                if(item == null) return;

                                if(item.getFID().equals("debug")) {
                                    action.getPlayer().getPlayer().sendMessage(EmojiList.SUCCESS_ICON + " " + ColorList.GRAY + "You clicked on " + item.getCustomName());
                                    action.getInventory().setTitle("Debug UI - "+item.getCustomName());

                                    action.setResult(ActionEventContext.ActionResult.DENY);
                                }
                            }, GeneralAction.PICKUP);

                    inventory.createContainer(4, 0, 6, 2).fill(Material.GOLDEN_APPLE);

                    for (RankImpl rank : rankFactory.search()) {
                        container.addItem(new ItemBuilder(Material.PLAYER_HEAD)
                                .setCustomName(rank.getName())
                                .setFID("debug")
                        );
                    }

                    inventory.view((Player) context.getSender());
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
