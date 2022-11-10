package com.meturum.centre.interfaces;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.meturum.centra.interfaces.Interface;
import com.meturum.centra.interfaces.Position;
import com.meturum.centra.interfaces.container.Container;
import com.meturum.centra.interfaces.item.ItemWrapper;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContainerImpl implements Container {

    private final Inventory inventory;

    private final LinkedList<Integer> slots = new LinkedList<>();

    private final List<InventoryAction> disabledActions = new LinkedList<>();

    public ContainerImpl(@NotNull Inventory inventory, @NotNull List<Integer> slots) {
        this.inventory = inventory;
        this.slots.addAll(slots);
    }

    public ContainerImpl(@NotNull Inventory inventory, int y, int height, int width, int x) {
        if(x < 0 || x > width)
            throw new IllegalArgumentException("Invalid container dimensions, x must be between 0 and "+width);

        if(y < 0 || y > height)
            throw new IllegalArgumentException("Invalid container dimensions, y must be between 0 and "+height);

        if(height < Interface.MINIMUM_HEIGHT || height > Interface.MAXIMUM_HEIGHT)
            throw new IllegalArgumentException("Invalid container dimensions, height must be between "+Interface.MINIMUM_HEIGHT+" and "+Interface.MAXIMUM_HEIGHT);

        if(width < Interface.MINIMUM_WIDTH || width > Interface.MAXIMUM_WIDTH)
            throw new IllegalArgumentException("Invalid container dimensions, width must be between "+Interface.MINIMUM_WIDTH+" and "+Interface.MAXIMUM_WIDTH);

        int cursor = y * 9 + x; // cursor is the position of the first item in the container.
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                slots.add(cursor);
                cursor++;
            }

            cursor += 9 - width;
        }

        this.inventory = inventory;
    }

    @Override
    public @Nullable ItemWrapper getItem(int slot) throws IllegalArgumentException {
        if(!slots.contains(slot))
            throw new IllegalArgumentException("Slot " + slot + " is not part of this container.");

        ItemStack bukkitItem = inventory.getItem(slot);
        if(bukkitItem == null) return null;

        if(bukkitItem instanceof ItemWrapper)
            return (ItemWrapper) bukkitItem;

        ItemWrapper item = ItemWrapper.of(bukkitItem);
        setItem(item, slot);

        return item;
    }

    @Override
    public @Nullable ItemWrapper getItem(@NotNull Position position) throws IllegalArgumentException {
        return getItem(position.toInt());
    }

    @Override
    public boolean containsItem(@NotNull ItemWrapper item) {
        for(Map.Entry<Integer, ItemWrapper> entry : getContents().entrySet()) {
            if(entry.getValue().equals(item)) return true;
        }

        return false;
    }

    @Override
    public boolean containsItem(int slot) {
        return getItem(slot) != null;
    }

    @Override
    public boolean containsItem(@NotNull Position position) {
        return containsItem(position.toInt());
    }

    public ContainerImpl setItem(@NotNull ItemWrapper item, @NotNull Position position) throws IllegalArgumentException {
        int posInt = position.toInt();

        if(!slots.contains(posInt))
            throw new IllegalArgumentException("Position " + position + " is not part of this container.");

        item.setPosition(position);
        inventory.setItem(posInt, item);

        return this;
    }

    @Override
    public ContainerImpl setItem(@NotNull ItemWrapper item, int slot) throws IllegalArgumentException {
        return setItem(item, Position.of(slot));
    }

    @Override
    public ContainerImpl addItem(@NotNull ItemWrapper item) throws IllegalArgumentException {
        int cursor = -1;

        for(int position : slots) {
            if(getItem(position) == null) {
                cursor = position;
                break;
            }
        }

        if(cursor == -1)
            throw new IllegalArgumentException("Unable to add item, container is full.");

        return setItem(item, cursor);
    }

    @Override
    public List<ItemWrapper> addItems(@NotNull ItemWrapper... items) {
        List<ItemWrapper> leftovers = new LinkedList<>();

        for(ItemWrapper item : items) {
            try {
                addItem(item);
            } catch(IllegalArgumentException e) {
                leftovers.add(item);
            }
        }

        return leftovers;
    }

    @Override
    public void removeItem(@NotNull ItemWrapper item) {
        inventory.remove(item);
    }

    @Override
    public void clear(int slot) {
        if(!slots.contains(slot))
            throw new IllegalArgumentException("Slot " + slot + " is not part of this container.");

        inventory.clear(slot);
    }

    @Override
    public void clear(@NotNull Position position) {
        clear(position.toInt());
    }

    @Override
    public void clear() {
        for (int position : slots) {
            clear(position);
        }
    }

    @Override
    public void disableAction(@NotNull InventoryAction action) {
        if(!disabledActions.contains(action))
            disabledActions.add(action);
    }

    @Override
    public @NotNull LinkedList<Integer> getSlots() {
        return slots;
    }

    @Override
    public @NotNull LinkedHashMap<Integer, ItemWrapper> getContents() {
        LinkedHashMap<Integer, ItemWrapper> contents = new LinkedHashMap<>();

        for(int slot : slots) {
            contents.put(slot, getItem(slot));
        }

        return contents;
    }

    public static ContainerImpl of(@NotNull Inventory inventory, Integer... slots) {
        return new ContainerImpl(inventory, List.of(slots));
    }

}
