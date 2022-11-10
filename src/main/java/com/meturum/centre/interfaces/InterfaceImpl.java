package com.meturum.centre.interfaces;

import com.google.common.collect.ImmutableList;
import com.meturum.centra.ColorList;
import com.meturum.centra.interfaces.Position;
import com.meturum.centra.sessions.Session;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftInventoryCustom;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.meturum.centra.interfaces.Interface;
import com.meturum.centra.interfaces.container.Container;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InterfaceImpl implements Interface {

    private final Inventory inventory;
    private final List<Container> containers = new ArrayList<>();
    private @NotNull String title = "Interface@"+hashCode();
    private @Nullable String overlay;
    private final List<ActionLambda> actionLambdas = new ArrayList<>();

    private final InterfaceSettings settings = new InterfaceSettings(false);

    public InterfaceImpl(@NotNull Inventory inventory, @NotNull InterfaceManager manager) {
        this.inventory = inventory;

        manager.register(this);
    }

    public InterfaceImpl(int height, @NotNull InterfaceManager manager) {
        this(Bukkit.createInventory(null, height * 9, "Interface"), manager);

        if(height < MINIMUM_HEIGHT || height > MAXIMUM_HEIGHT)
            throw new IllegalArgumentException("Height must be between " + MINIMUM_HEIGHT + " and " + MAXIMUM_HEIGHT + ".");
    }

    public @NotNull Inventory getLinkedInventory() {
        return inventory;
    }

    @Override
    public int getHeight() {
        return inventory.getSize() / 9;
    }

    @Override
    public Container getContainer(int position) {
        for(Container container : containers) {
            for(Integer slots : container.getContents().keySet()) {
                if(slots == position) return container;
            }
        }

        return null;
    }

    @Override
    public Container getContainer(@NotNull Position position) {
        return getContainer(position.toInt());
    }

    public InterfaceImpl addContainer(@NotNull Container container) throws IllegalArgumentException {
        if(containsContainer(container))
            throw new IllegalArgumentException("Unable to add container, the interface already contains the container.");

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
    public boolean containsContainer(@NotNull Container container) {
        return containers.contains(container);
    }

    @Override
    public boolean removeContainer(@NotNull Container container) {
        if(!containsContainer(container))
            return false;

        containers.remove(container);
        return true;
    }

    @Override
    public @NotNull ImmutableList<Container> getContainers() {
        return ImmutableList.copyOf(containers);
    }

    @Override
    public @NotNull ItemStack[] getContents() {
        return inventory.getContents();
    }

    @Override
    public boolean contains(@NotNull ItemStack item) {
        return inventory.contains(item);
    }

    @Override
    public boolean contains(int position) {
        return inventory.getItem(position) != null;
    }

    @Override
    public boolean isEmpty() {
        return inventory.isEmpty();
    }

    @Override
    public @NotNull String getTitle() {
        return title;
    }

    @Override
    public InterfaceImpl setTitle(@Nullable String title) {
        if(title == null) title = "Interface@"+hashCode();
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
    public InterfaceImpl setOverlay(@Nullable String overlay) {
        this.overlay = overlay;

        update();
        return this;
    }

    public @NotNull String getFormattedTitle() {
        String offset = "\uF80C\uF80A\uF808\uF802"; // -202px
        return (hasOverlay() ? ColorList.WHITE + "\uF808"+getOverlay()+offset : "") + ColorList.DARK_GRAY + getTitle();
    }

    @Override
    public InterfaceImpl interacts(@NotNull ActionLambda lambda) {
        actionLambdas.add(lambda);

        return this;
    }

    public boolean reportInteraction(@NotNull ActionContext context) {
        for(ActionLambda lambda : actionLambdas) {
            if(lambda.run(context)) return true;
        }

        return false;
    }

    public @NotNull InterfaceSettings getSettings() {
        return settings;
    }

    @Override
    public InterfaceImpl view(@NotNull Player player, boolean force) throws IllegalStateException {
        if (player.getOpenInventory().getType().equals(InventoryType.PLAYER))
            if (force) player.getPlayer().closeInventory();
            else throw new IllegalStateException("Unable to open interface, the player is already viewing an inventory.");

        player.openInventory(inventory);
        return this;
    }

    @Override
    public InterfaceImpl view(@NotNull Player player) throws IllegalStateException {
        return view(player, false);
    }

    @Override
    public void close(@NotNull Player player) {
        if(inventory.getViewers().contains(player))
            player.closeInventory();
    }

    @Override
    public @NotNull ContainerImpl createContainer(@NotNull List<Integer> slots) throws IllegalArgumentException {
        ContainerImpl container = new ContainerImpl(inventory, slots);
        addContainer(container);

        return container;
    }

    @Override
    public @NotNull ContainerImpl createContainer(int x, int y, int height, int width) {
        ContainerImpl container = new ContainerImpl(inventory, y, height, width, x);
        addContainer(container);

        return container;
    }

    @Override
    public @NotNull ContainerImpl createContainer(int x, int y, int height) throws IllegalArgumentException {
        return createContainer(x, y, height, 9);
    }

    private void update() {
        // Server-sided
        CraftInventory inventory = (CraftInventory) getLinkedInventory();

        try {
            net.minecraft.world.Container minecraftInventory = inventory.getInventory();
            Field titleField = minecraftInventory.getClass().getDeclaredField("title");
            titleField.setAccessible(true);

            titleField.set(minecraftInventory, getFormattedTitle()); // MinecraftInventory.title (set)
        }catch (NoSuchFieldException | NullPointerException | SecurityException | IllegalAccessException exception) {
            throw new RuntimeException("Unable to update the title of the interface.", exception);
        }

        // Client-sided
        for(HumanEntity player : this.inventory.getViewers()) {
            ServerPlayer p = ((CraftPlayer) player).getHandle();
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(
                    p.containerMenu.containerId,
                    p.containerMenu.getType(),
                    Component.translatable(getFormattedTitle())
            );
            p.connection.send(packet);
        }
    }

    public static class InterfaceSettings {
        boolean allowMovement = false;

        public boolean ge
    }

}