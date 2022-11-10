package com.meturum.centra.interfaces.container;

import com.meturum.centra.interfaces.Position;
import com.meturum.centra.interfaces.item.ItemWrapper;
import org.bukkit.event.inventory.InventoryAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public interface Container {

    @Nullable ItemWrapper getItem(int slot) throws IllegalArgumentException;

    @Nullable ItemWrapper getItem(@NotNull Position position) throws IllegalArgumentException;

    boolean containsItem(@NotNull ItemWrapper item);

    boolean containsItem(int slot);

    boolean containsItem(@NotNull Position position);

    Container setItem(@NotNull ItemWrapper item, @NotNull Position position) throws IllegalArgumentException;

    Container setItem(@NotNull ItemWrapper item, int slot) throws IllegalArgumentException;

    Container addItem(@NotNull ItemWrapper item);

    List<ItemWrapper> addItems(@NotNull ItemWrapper... items);

    void removeItem(@NotNull ItemWrapper item);

    void clear(int slot);

    void clear(@NotNull Position position);

    void clear();

    void disableAction(@NotNull InventoryAction action);

    @NotNull LinkedList<Integer> getSlots();

    @NotNull LinkedHashMap<Integer, ItemWrapper> getContents();

}
