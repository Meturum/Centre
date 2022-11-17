package com.meturum.centre.inventory;

import com.meturum.centra.inventory.actions.GeneralAction;
import com.meturum.centre.inventory.action.ActionableImpl;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.meturum.centra.inventory.CustomInventory;
import com.meturum.centra.inventory.item.Position;
import com.meturum.centra.inventory.container.Container;
import com.meturum.centra.inventory.item.ItemBuilder;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ContainerImpl extends ActionableImpl implements Container {

    private final CustomInventoryImpl customInventory;
    private final Inventory inventory;
    private final LinkedList<Integer> slots = new LinkedList<>();


    public ContainerImpl(@NotNull CustomInventoryImpl inventory, @NotNull List<Integer> slots) {
        this.customInventory = inventory;
        this.inventory = inventory.getTopInventory();
        this.slots.addAll(slots);
    }

    public ContainerImpl(@NotNull CustomInventoryImpl inventory, int x, int y, int height, int width) {
        if(x < 0 || x > (9 - width))
            throw new IllegalArgumentException("Invalid container dimensions, x must be between 0 and "+width);

        if(y < 0 || y > (11 - height))
            throw new IllegalArgumentException("Invalid container dimensions, y must be between 0 and "+height);

        if(height < CustomInventory.MINIMUM_HEIGHT || height > CustomInventory.MAXIMUM_HEIGHT)
            throw new IllegalArgumentException("Invalid container dimensions, height must be between "+ CustomInventory.MINIMUM_HEIGHT+" and "+ CustomInventory.MAXIMUM_HEIGHT);

        if(width < CustomInventory.MINIMUM_WIDTH || width > CustomInventory.MAXIMUM_WIDTH)
            throw new IllegalArgumentException("Invalid container dimensions, width must be between "+ CustomInventory.MINIMUM_WIDTH+" and "+ CustomInventory.MAXIMUM_WIDTH);

        int cursor = y * 9 + x; // cursor is the position of the first item in the container.
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                slots.add(cursor);
                cursor++;
            }

            cursor += 9 - width;
        }

        this.customInventory = inventory;
        this.inventory = inventory.getTopInventory();
    }

    @Override
    public @Nullable ItemBuilder getItem(int slot) throws IllegalArgumentException {
        if(!slots.contains(slot))
            throw new IllegalArgumentException("Slot " + slot + " is not part of this container.");

        int position = customInventory.convertPosition(slot);

        Inventory srcInventory = customInventory.getSourceInventory(slot);
        if(srcInventory == null) return null;

        ItemStack bukkitItem = srcInventory.getItem(position);
        return bukkitItem != null ? new ItemBuilder(bukkitItem) : null;
    }

    @Override
    public @Nullable ItemBuilder getItem(@NotNull Position position) throws IllegalArgumentException {
        return getItem(position.toInt());
    }

    @Override
    public boolean containsItem(@NotNull ItemBuilder item) {
        for(Map.Entry<Integer, ItemBuilder> entry : getContents().entrySet()) {
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

    public ContainerImpl setItem(@NotNull ItemBuilder item, @NotNull Position position) throws IllegalArgumentException {
        int posInt = position.toInt(); // the original position

        if(!slots.contains(posInt))
            throw new IllegalArgumentException("Slot " + posInt + " is not part of this container.");

        int slot = customInventory.convertPosition(posInt); // the position in the top inventory or player inventory
        int trueSlot = posInt - customInventory.getTopInventory().getSize(); // the position in the bottom inventory

        ItemStack bukkitItem = item.build();
        Inventory srcInventory = customInventory.getTopInventory();

        if(slot != posInt && customInventory.isMaximized()) {
           srcInventory = customInventory.getBottomInventory();

           srcInventory.setItem(trueSlot, bukkitItem);
           customInventory.getViewers().forEach(p -> p.getInventory().setItem(slot, bukkitItem));
        } else {
            srcInventory.setItem(slot, item.build());
        }

        item.setPosition(position);
        return this;
    }

    @Override
    public ContainerImpl setItem(@NotNull ItemBuilder item, int slot) throws IllegalArgumentException {
        return setItem(item, Position.of(slot));
    }

    @Override
    public ContainerImpl addItem(@NotNull ItemBuilder item) throws IllegalArgumentException {
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
    public List<ItemBuilder> addItems(@NotNull ItemBuilder... items) {
        List<ItemBuilder> leftovers = new LinkedList<>();

        for(ItemBuilder item : items) {
            try {
                addItem(item);
            } catch(IllegalArgumentException e) {
                leftovers.add(item);
            }
        }

        return leftovers;
    }

    @Override
    public ContainerImpl fill(@NotNull Material material) {
        for (int slot : slots) {
            setItem(new ItemBuilder(material), slot);
        }

        return this;
    }

    @Override
    public void clear(int slot) {
        if(!slots.contains(slot))
            throw new IllegalArgumentException("Slot " + slot + " is not part of this container.");

        int position = customInventory.convertPosition(slot);
        Inventory srcInventory = customInventory.getSourceInventory(slot);
        if(srcInventory == null) return;

        if(srcInventory.equals(customInventory.getBottomInventory())) {
            customInventory.getViewers().forEach(p -> p.getInventory().clear(position));
        }

        srcInventory.clear(position);
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
    public @NotNull LinkedList<Integer> getSlots() {
        return slots;
    }

    @Override
    public @NotNull LinkedHashMap<Integer, ItemBuilder> getContents() {
        LinkedHashMap<Integer, ItemBuilder> contents = new LinkedHashMap<>();

        for(int slot : slots) {
            contents.put(slot, getItem(slot));
        }

        return contents;
    }

    // --- Only overriding these methods to change the return type.

    public @NotNull ContainerImpl setAllowedActions(@NotNull InventoryAction... actions) {
        super.setAllowedActions(actions);

        return this;
    }

    public @NotNull ContainerImpl setAllowedActions(@NotNull GeneralAction... actions) {
        super.setAllowedActions(actions);

        return this;
    }

    @Override
    public ContainerImpl setAllowDragging(boolean allowDragging) {
        super.setAllowDragging(allowDragging);

        return this;
    }

    @Override
    public ContainerImpl interacts(@NotNull ActionLambda lambda) {
        super.interacts(lambda);

        return this;
    }

    @Override
    public ContainerImpl interacts(@NotNull ActionLambda lambda, InventoryAction... applicableActions) {
        super.interacts(lambda, applicableActions);

        return this;
    }

    @Override
    public ContainerImpl interacts(@NotNull ActionLambda lambda, GeneralAction... applicableActions) {
        super.interacts(lambda, applicableActions);

        return this;
    }

    // --- End of overriding methods.

    public static ContainerImpl of(@NotNull CustomInventoryImpl inventory, Integer... slots) {
        return new ContainerImpl(inventory, List.of(slots));
    }

}
