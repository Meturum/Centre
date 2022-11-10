package com.meturum.centra.interfaces;

import com.google.common.collect.ImmutableList;
import com.meturum.centra.interfaces.container.Container;
import com.meturum.centra.interfaces.item.ItemWrapper;
import com.meturum.centra.sessions.Session;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public interface Interface {

    int MINIMUM_HEIGHT = 1; int MAXIMUM_HEIGHT = 6;
    int MINIMUM_WIDTH = 1; int MAXIMUM_WIDTH = 9;

    int getHeight();

    Container getContainer(int position);

    Container getContainer(@NotNull Position position);

    /**
     * Checks if the interface contains a container.
     *
     * @param container The container to check.
     * @return true if the interface contains the container, false otherwise.
     */
    boolean containsContainer(@NotNull Container container);

    boolean removeContainer(@NotNull Container container);

    @NotNull ImmutableList<Container> getContainers();

    @NotNull ItemStack[] getContents();

    boolean contains(@NotNull ItemStack item);

    boolean contains(int position);

    boolean isEmpty();

    @NotNull String getTitle();

    Interface setTitle(@Nullable String title);

    @Nullable String getOverlay();

    boolean hasOverlay();

    @NotNull String getFormattedTitle();

    Interface setOverlay(@Nullable String overlay);

    Interface interacts(@NotNull ActionLambda lambda);

    Interface view(@NotNull Player player, boolean force) throws IllegalStateException;

    Interface view(@NotNull Player player) throws IllegalStateException;

    void close(@NotNull Player player);

    @NotNull Container createContainer(@NotNull List<Integer> slots) throws IllegalArgumentException;

    @NotNull Container createContainer(int x, int y, int height, int width) throws IllegalArgumentException;

    @NotNull Container createContainer(int x, int y, int height) throws IllegalArgumentException;

    interface ActionLambda {
        boolean run(@NotNull ActionContext context);
    }

    record ActionContext(@NotNull InventoryEvent event, @NotNull Interface interfaze, @Nullable Container container, @Nullable ItemWrapper item, @NotNull Session session) { }

}
