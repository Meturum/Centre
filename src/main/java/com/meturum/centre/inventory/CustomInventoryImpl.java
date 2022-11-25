package com.meturum.centre.inventory;

import com.google.common.collect.ImmutableList;
import com.meturum.centra.ColorList;
import com.meturum.centra.inventory.item.Position;
import com.meturum.centra.inventory.CustomInventoryView;
import com.meturum.centra.inventory.item.ItemBuilder;
import com.meturum.centre.inventory.action.ActionEventContextImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import com.meturum.centra.inventory.CustomInventory;
import com.meturum.centra.inventory.container.Container;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomInventoryImpl implements CustomInventory {

    private final InventoryManagerImpl manager;

    private final Inventory topInventory;
    private @Nullable Inventory bottomInventory;
    private final List<ContainerImpl> containers = new ArrayList<>();
    private @NotNull String title = "Interface@"+hashCode();
    private @Nullable String overlay;
    private final List<CustomInventoryViewImpl> viewers = new ArrayList<>();

    private boolean usePlayerInventory = false;

    public CustomInventoryImpl(@NotNull final Inventory topInventory, @Nullable final Inventory bottomInventory, @NotNull final InventoryManagerImpl manager) {
        this.topInventory = topInventory;
        this.manager = manager;

        if(bottomInventory != null) {
            if(bottomInventory.getSize() != 9 * 4)
                throw new IllegalArgumentException("Bottom inventory must be 4 rows. (To mimic the player inventory)");

            this.bottomInventory = bottomInventory;
        }

        manager.register(this);
    }

    public CustomInventoryImpl(@NotNull final Inventory topInventory, @NotNull final InventoryManagerImpl manager) {
        this(topInventory, null, manager);
    }

    public CustomInventoryImpl(final int height, @NotNull final InventoryManagerImpl manager) {
        this(Bukkit.createInventory(null, height * 9, "Interface"), null, manager);

        if(height < MINIMUM_HEIGHT || height > MAXIMUM_HEIGHT)
            throw new IllegalArgumentException("Height must be between " + MINIMUM_HEIGHT + " and " + MAXIMUM_HEIGHT + ".");
    }

    public @NotNull Inventory getTopInventory() {
        return topInventory;
    }

    public @Nullable Inventory getSourceInventory(final int position) {
        int truePosition = convertPosition(position);

        Inventory inventory = getTopInventory();
        if(truePosition != position) inventory = getBottomInventory();

        return inventory;
    }

    public @Nullable Inventory getBottomInventory() {
        return bottomInventory;
    }

    @Override
    public int getHeight() {
        return topInventory.getSize() / 9;
    }

    @Override
    public ContainerImpl getContainer(final int position) {
        for(ContainerImpl container : containers) {
            for(Integer slots : container.getContents().keySet()) {
                if(slots == position) return container;
            }
        }

        return null;
    }

    @Override
    public ContainerImpl getContainer(@NotNull final Position position) {
        return getContainer(position.toInt());
    }

    public CustomInventoryImpl addContainer(@NotNull final ContainerImpl container) throws IllegalArgumentException {
        // Make sure we don't have this container already
        if(containsContainer(container))
            throw new IllegalArgumentException("Unable to add container, the interface already contains the container.");

        // Make sure the container will fit in the inventory.
        int lastIndex = container.getSlots().getLast();
        int maxSize = topInventory.getSize() + (isMaximized() ? bottomInventory.getSize() : 0);

        if(lastIndex >= maxSize)
            throw new IllegalArgumentException("Unable to add container, the container is too large for the interface.");

        // Make sure the container doesn't overlap with any other containers.
        for(Container compare : containers) {
            for(int position : compare.getSlots()) {
                if(container.getSlots().contains(position))
                    throw new IllegalArgumentException("Unable to add container, a container already contains the position " + position + ".");
            }
        }

        containers.add(container);
        return this;
    }

    @Override
    public boolean containsContainer(@NotNull final Container container) {
        return containers.contains((ContainerImpl) container);
    }

    @Override
    public boolean removeContainer(@NotNull final Container container) {
        if(!containsContainer(container))
            return false;

        containers.remove((ContainerImpl) container);
        return true;
    }

    @Override
    public @NotNull ImmutableList<Container> getContainers() {
        return ImmutableList.copyOf(containers);
    }

    @Override
    public @NotNull ItemStack[] getContents() {
        return topInventory.getContents();
    }

    @Override
    public boolean contains(@NotNull final ItemStack item) {
        return topInventory.contains(item);
    }

    @Override
    public boolean isEmpty(final int position) {
        return topInventory.getItem(position) != null;
    }

    @Override
    public boolean isEmpty() {
        return topInventory.isEmpty();
    }

    @Override
    public @NotNull String getTitle() {
        return title;
    }

    @Override
    public CustomInventoryImpl setTitle(@Nullable final String title) {
        if(title == null) return setTitle("Interface@"+hashCode()); // Default the title.
        this.title = title;

        update();
        return this;
    }

    @Override
    public @Nullable String getOverlay() {
        return overlay;
    }

    @Override
    public boolean hasOverlay() {
        return overlay != null && !overlay.isEmpty();
    }

    @Override
    public CustomInventoryImpl setOverlay(@Nullable final String overlay) {
        this.overlay = overlay;

        update();
        return this;
    }

    public @NotNull String getFormattedTitle() {
        String offset = "\uF80C\uF80A\uF808\uF802"; // -202px
        return (hasOverlay() ? ColorList.WHITE + "\uF808"+getOverlay()+offset : "") + ColorList.DARK_GRAY + getTitle();
    }

    @Override
    public boolean isViewing(@NotNull final Player player) {
        for(CustomInventoryView cursor : viewers) {
            if(cursor.getOwner().getUniqueId().equals(player.getUniqueId())) return true;
        }

        return false;
    }

    @Override
    public CustomInventoryImpl view(@NotNull final Player player, final boolean force) throws IllegalStateException {
        if (player.getPlayer().getOpenInventory().getType().equals(InventoryType.PLAYER))
            if (force) player.getPlayer().closeInventory();
            else throw new IllegalStateException("Unable to open interface, the player is already viewing an inventory.");

        if(isViewing(player))
            throw new IllegalStateException("Unable to open interface, the player is already viewing the interface.");

        CustomInventoryViewImpl view = new CustomInventoryViewImpl(this, player);
        viewers.add(view);

        player.getPlayer().openInventory(topInventory);

        if(usePlayerInventory) {
            view.upload();
        }

        return this;
    }

    @Override
    public CustomInventoryImpl view(@NotNull final Player player) throws IllegalStateException {
        return view(player, false);
    }

    public void close(@NotNull final Player player, final boolean force) {
        if(!isViewing(player)) return;

        CustomInventoryViewImpl view = getViewerCursor(player);
        viewers.remove(view);

        if(usePlayerInventory) {
            view.download();
        }

        if(force) player.closeInventory();
    }

    @Override
    public void close(@NotNull final Player player) {
        close(player, true);
    }

    public CustomInventoryViewImpl getViewerCursor(@NotNull final Player player) {
        for(CustomInventoryViewImpl cursor : viewers) {
            if(cursor.getOwner().getUniqueId().equals(player.getUniqueId())) return cursor;
        }

        return null;
    }

    @Override
    public @NotNull List<Player> getViewers() {
        List<Player> viewers = new ArrayList<>();

        for(HumanEntity entity : topInventory.getViewers()) {
            viewers.add((Player) entity);
        }

        return viewers;
    }

    public @NotNull List<CustomInventoryView> getViewerCursors() {
        return ImmutableList.copyOf(viewers);
    }

    public @NotNull CustomInventoryImpl setMaximized() {
        if(usePlayerInventory) return this;

        bottomInventory = Bukkit.createInventory(null, 9 * 4, "");

        for(CustomInventoryViewImpl view : viewers) {
            view.upload();
        }

        usePlayerInventory = true;
        return this;
    }

    public @NotNull CustomInventoryImpl setMinimized() {
        if(!usePlayerInventory) return this;

        bottomInventory = null;

        for (CustomInventoryViewImpl view : viewers) {
            view.download();
        }

        usePlayerInventory = false;
        return this;
    }

    public boolean isMaximized() {
        return usePlayerInventory;
    }

    public boolean isMinimized() {
        return !usePlayerInventory;
    }

    public int convertPosition(final int index) {
        int numInTop = topInventory.getSize();

        if (index < numInTop) {
            return index;
        }

        int slot = index - numInTop;

        // 27 = 36 - 9
        if (slot >= 27) {
            // Put into hotbar section
            slot -= 27;
        } else {
            // Take out of hotbar section
            // 9 = 36 - 27
            slot += 9;
        }

        return slot;
    }

    @Override
    public @NotNull ContainerImpl createContainer(@NotNull final List<Integer> slots) throws IllegalArgumentException {
        ContainerImpl container = new ContainerImpl(this, slots);
        addContainer(container);

        return container;
    }

    @Override
    public @NotNull ContainerImpl createContainer(final int x, final int y, final int height, final int width) throws IllegalArgumentException {
        ContainerImpl container = new ContainerImpl(this, x, y, height, width);
        addContainer(container);

        return container;
    }

    @Override
    public @NotNull ContainerImpl createContainer(final int x, final int y, final int height) throws IllegalArgumentException {
        return createContainer(x, y, height, 9);
    }

    public void reportInteraction(@NotNull final ActionEventContextImpl context) {
        ContainerImpl container = context.getContainer();
        if(container != null) container.reportInteraction(context);
    }

    private void update() {
        // Server-sided
        CraftInventory inventory = (CraftInventory) getTopInventory();

        try {
            net.minecraft.world.Container minecraftInventory = inventory.getInventory();
            Field titleField = minecraftInventory.getClass().getDeclaredField("title");
            titleField.setAccessible(true);

            titleField.set(minecraftInventory, getFormattedTitle()); // MinecraftInventory.title (set)
        }catch (NoSuchFieldException | NullPointerException | SecurityException | IllegalAccessException exception) {
            throw new RuntimeException("Unable to update the title of the interface.", exception);
        }

        // Client-sided
        for(HumanEntity player : this.topInventory.getViewers()) {
            ServerPlayer p = ((CraftPlayer) player).getHandle();
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                    p.containerMenu.containerId,
                    p.containerMenu.getType(),
                    Component.translatable(getFormattedTitle())
            );
            p.connection.send(packet);
        }
    }

    @Override
    public void destroy() {
        for (CustomInventoryView cursor : getViewerCursors()) {
            close(cursor.getOwner());
        }

        manager.unregister(this);
    }

    public static class CustomInventoryViewImpl implements CustomInventoryView {

        private final Player owner;
        private final CustomInventoryImpl inventory;
        private @Nullable ItemBuilder cursor;
        private @Nullable Container container;

        private @Nullable ItemStack[] bottomContents;

        public CustomInventoryViewImpl(@NotNull final CustomInventoryImpl inventory, final @NotNull Player owner) {
            this.owner = owner;
            this.inventory = inventory;
        }

        @Override
        public @NotNull Player getOwner() {
            return owner;
        }

        @Override
        public @NotNull CustomInventory getInventory() {
            return inventory;
        }

        @Override
        public @Nullable ItemBuilder getCursor() {
            return cursor;
        }

        @Override
        public @Nullable Container getContainer() {
            return container;
        }

        public void setCursor(@Nullable final ItemBuilder cursor, @Nullable final Container container) {
            this.cursor = cursor;
            this.container = container;
        }

        @Override
        public @Nullable ItemStack[] getBottomContents() {
            return bottomContents;
        }

        private void setBottomContents(@Nullable final ItemStack[] bottomContents) {
            this.bottomContents = bottomContents;
        }

        public void upload() {
            PlayerInventory inventory = owner.getInventory();
            setBottomContents(inventory.getContents());

            inventory.clear();

            ItemStack[] contents = this.inventory.getBottomInventory().getContents();
            ItemStack[] neoContents = new ItemStack[contents.length];

            for (int i = 0; i < contents.length; i++) {
                int viewIndex = i + this.inventory.getTopInventory().getSize();
                int absIndex = this.inventory.convertPosition(viewIndex);

                neoContents[absIndex] = contents[i];
            }

            inventory.setContents(neoContents);
        }

        public void download() {
            PlayerInventory inventory = owner.getInventory();

            inventory.clear();
            inventory.setContents(getBottomContents());

            bottomContents = null;
        }

    }

}