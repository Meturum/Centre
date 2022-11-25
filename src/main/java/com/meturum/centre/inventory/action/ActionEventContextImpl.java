package com.meturum.centre.inventory.action;

import com.meturum.centra.inventory.actions.ActionEventContext;
import com.meturum.centra.inventory.item.ItemBuilder;
import com.meturum.centra.inventory.item.Position;
import com.meturum.centre.inventory.ContainerImpl;
import com.meturum.centre.inventory.CustomInventoryImpl;
import com.meturum.centre.sessions.SessionImpl;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ActionEventContextImpl implements ActionEventContext {

    private final CustomInventoryImpl inventory;
    private final ContainerImpl container;
    private final SessionImpl player;
    private final @Nullable ItemBuilder currentItem;
    private final Position position;
    private final InventoryClickEvent bukkit_context;
    private ActionResult result = ActionResult.DEFAULT;

    public ActionEventContextImpl(@NotNull final CustomInventoryImpl inventory, @Nullable final ContainerImpl container, @NotNull final SessionImpl player, @Nullable final ItemBuilder currentItem, @NotNull final Position position, @NotNull final InventoryClickEvent bukkit_context) {
        this.inventory = inventory;
        this.container = container;
        this.player = player;
        this.currentItem = currentItem;
        this.position = position;
        this.bukkit_context = bukkit_context;
    }

    public @NotNull CustomInventoryImpl getInventory() {
        return inventory;
    }

    public @NotNull ContainerImpl getContainer() {
        return container;
    }

    public @NotNull SessionImpl getPlayer() {
        return player;
    }

    public @Nullable ItemBuilder getCurrentItem() {
        return currentItem;
    }

    public @NotNull Position getPosition() {
        return position;
    }

    public @NotNull InventoryClickEvent getBukkitContext() {
        return bukkit_context;
    }

    public @NotNull ActionResult getResult() {
        return result;
    }

    @Override
    public void setResult(@NotNull final ActionResult result) {
        this.result = result;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(final boolean cancel) {
        setResult(cancel ? ActionResult.DENY : ActionResult.ALLOW);
    }

}
