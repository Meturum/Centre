package com.meturum.centre.inventory;

import com.meturum.centra.inventory.InventoryManager;
import com.meturum.centra.inventory.actions.ActionEventContext;
import com.meturum.centra.inventory.item.Position;
import com.meturum.centra.inventory.item.ItemBuilder;
import com.meturum.centre.Centre;
import com.meturum.centre.inventory.action.ActionEventContextImpl;
import com.meturum.centre.sessions.SessionFactoryImpl;
import com.meturum.centre.sessions.SessionImpl;
import com.meturum.centre.util.SystemImpl;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class InventoryManagerImpl extends SystemImpl implements InventoryManager {

    private final SessionFactoryImpl sessionFactory;

    private final List<CustomInventoryImpl> interfaces = new ArrayList<>();

    public InventoryManagerImpl(@NotNull final Centre centre, @NotNull final SessionFactoryImpl sessionFactory) {
        super(centre);

        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void _stop() {
        for (CustomInventoryImpl inventory : interfaces) {
            inventory.destroy();
        }
    }

    public void register(@NotNull final CustomInventoryImpl ui) {
        if(interfaces.contains(ui)) return;
        interfaces.add(ui);
    }

    public CustomInventoryImpl search(@NotNull final Inventory inventory) {
        return interfaces.stream().filter(ui -> ui.getTopInventory().equals(inventory)).findFirst().orElse(null);
    }

    public void unregister(@NotNull final CustomInventoryImpl ui) {
        if(!interfaces.contains(ui)) return;

        ui.getTopInventory().getViewers().forEach(HumanEntity::closeInventory);
        interfaces.remove(ui);
    }

    @EventHandler
    public void onICE(@NotNull final InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if (action == InventoryAction.NOTHING || action == InventoryAction.UNKNOWN)
            return; // These actions are useless.

        final Player player = (Player) event.getWhoClicked();
        final SessionImpl session = sessionFactory.search(player);
        if (session == null) return;

        CustomInventoryImpl ui = search(event.getView().getTopInventory()); // Our cached inventory is only the top inventory.
        if (ui == null) return;

        Position position = Position.of(event.getRawSlot());
        ContainerImpl container = ui.getContainer(position);

        // All this does is verify that the specified item is correct.
        // It checks the following:
        // 1. The item exist in the actual inventory. (currentItem != null && !currentItem.getType().isAir())
        // 2. If event.getCurrentItem() is null, then the item is not in the inventory. And it must be in the cursor.
        // 3. After verifying the item, it converts it to our ItemWrapper.

        final int abs_position = event.getView().convertSlot(event.getRawSlot());
        final boolean isTopInventory = event.getRawSlot() == abs_position;
        if(!isTopInventory && ui.isMinimized())
            return; // If the inventory is not maximized, then we don't want to interfere with the player interacting with the bottom inventory.

        final ItemStack currentItemVerifier = event.getView().getItem(event.getRawSlot());
        final ItemStack currentItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        ItemBuilder item = currentItemVerifier != null && !currentItemVerifier.getType().isAir()
                ? currentItem != null && !currentItem.getType().isAir()
                    ? new ItemBuilder(currentItem)
                    : null
                : cursorItem != null && !cursorItem.getType().isAir()
                    ? new ItemBuilder(cursorItem)
                    : null;

        final ActionEventContextImpl context = new ActionEventContextImpl(ui, container, session, item, position, event);
        final boolean containerLevel;

        if (container != null) {
            container.reportInteraction(context);
            containerLevel = !container.isAllowedAction(action);
        } else containerLevel = true; // It's outside the container, so we don't want to allow any actions.

        final boolean userLevel = context.getResult().getValue();
        final boolean useUserLevel = context.getResult() != ActionEventContext.ActionResult.DEFAULT;
        final boolean itemLevel = item.isLocked();

        // If the user has a lambda, we want to rely on userLevel. Otherwise, if uiLevel or containerLevel is true, we want to cancel the event.
        // Even if the user attempts to allow the event, if itemLevel is true, we want to cancel the event.
        // TLDR: userLevel overrides uiLevel and containerLevel. itemLevel override userLevel (Inherently overriding uiLevel and containerLevel).

        if (!((useUserLevel ? userLevel : containerLevel) || itemLevel)) return;

        final CustomInventoryImpl.CustomInventoryViewImpl view = ui.getViewerCursor(player);
        if(view != null) view.setCursor(item, container);

        event.setResult(InventoryClickEvent.Result.DENY);
    }

    @EventHandler
    public void onIDE(@NotNull final InventoryDragEvent event) {
        CustomInventoryImpl ui = search(event.getView().getTopInventory());
        if (ui == null) return;

        boolean cancel = false;
        for(Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            int trueSlot = event.getView().convertSlot(entry.getKey());
            if(entry.getKey() != trueSlot && ui.isMinimized())
                continue; // If the slot is not in the top inventory and the inventory is minimized, we don't care.

            Position position = Position.of(entry.getKey());
            ContainerImpl container = ui.getContainer(position);

            int count = entry.getValue().getAmount(); // Figure out the right action.
            InventoryAction action = count == 1 ? InventoryAction.PLACE_ONE : InventoryAction.PLACE_SOME;

            // if it's inside a container, we rely on containerLevel
            // if it's not inside a container, we rely on uiLevel
            // itemLevel overrides both

            boolean useContainerLevel = container != null;
            boolean containerLevel = container != null && !container.isAllowedAction(action) && !container.isAllowDragging();
            boolean itemLevel = new ItemBuilder(entry.getValue()).isLocked();

            cancel = !useContainerLevel || containerLevel || itemLevel;
            if(cancel) break;
        }

        event.setCancelled(cancel);
    }

    @EventHandler
    public void onICE(@NotNull final InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        CustomInventoryImpl ui = search(event.getInventory());
        if(ui == null) return; // Not an interface.

        ui.close(player, false);
    }

    @EventHandler
    public void onPQE(@NotNull final PlayerQuitEvent event) {
        Player player = event.getPlayer();
        interfaces.forEach(ui -> ui.close(player));
    }

    @Override
    public @NotNull CustomInventoryImpl createInventory(final int height) {
        return new CustomInventoryImpl(height, this);
    }

    @Override
    public @NotNull CustomInventoryImpl createInventory(@NotNull final Inventory inventory) {
        return new CustomInventoryImpl(inventory, this);
    }


}
