package com.meturum.centre.interfaces;

import com.meturum.centra.interfaces.Position;
import com.meturum.centra.interfaces.container.Container;
import com.meturum.centra.interfaces.item.ItemWrapper;
import com.meturum.centra.sessions.Session;
import com.meturum.centra.sessions.SessionFactory;
import com.meturum.centre.Centre;
import com.meturum.centre.util.SystemImpl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.meturum.centra.interfaces.Interface;

import java.util.ArrayList;
import java.util.List;
public class InterfaceManager extends SystemImpl {

    private final SessionFactory sessionFactory;

    private final List<InterfaceImpl> interfaces = new ArrayList<>();

    public InterfaceManager(@NotNull Centre centre, @NotNull SessionFactory sessionFactory) {
        super(centre);

        this.sessionFactory = sessionFactory;
    }

    public void register(@NotNull InterfaceImpl ui) {
        if(interfaces.contains(ui)) return;
        interfaces.add(ui);
    }

    public InterfaceImpl search(@NotNull Inventory inventory) {
        return interfaces.stream().filter(ui -> ui.getLinkedInventory().equals(inventory)).findFirst().orElse(null);
    }

    public void unregister(@NotNull InterfaceImpl ui) {
        if(!interfaces.contains(ui)) return;

        ui.getLinkedInventory().getViewers().forEach(HumanEntity::closeInventory);
        interfaces.remove(ui);
    }

    @EventHandler
    public void onICE(InventoryClickEvent event) {
        Session session = sessionFactory.search((Player) event.getWhoClicked());
        if(session == null) return;

        InterfaceImpl ui = search(event.getInventory());
        if(ui == null) return; // Not an interface.

        Position position = Position.of(event.getRawSlot());
        Container container = ui.getContainer(position);

        ItemWrapper item = container.getItem(position);
        if(item != null) {
            ItemStack spigotItem = event.getCurrentItem();

            if(!(spigotItem instanceof ItemWrapper) && !spigotItem.isSimilar(item))
                throw new RuntimeException("Unable to verify interface, the item in the inventory is not the same as the item in the container.");
        }

        event.setCancelled(switch (event.getAction()) {
            case NOTHING, UNKNOWN -> false;
            case CLONE_STACK, DROP_ONE_CURSOR, DROP_ALL_CURSOR -> true;
            default -> {
                ui.getSettings().allowMovement();

                Interface.ActionContext context = new Interface.ActionContext(event, ui, container, item, session);
                yield ui.reportInteraction(context);
            }
        });
    }

    @EventHandler
    public void onIDE(InventoryDragEvent event) {

    }

    @EventHandler
    public void onICE(InventoryCloseEvent event) {

    }

}
