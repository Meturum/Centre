package com.meturum.centra.interfaces.item;

import com.meturum.centra.interfaces.Position;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ItemWrapper extends ItemStack {

    private @Nullable String FID;

    private @Nullable Position position;

    public ItemWrapper(@NotNull Material material) {
        super(material);
    }

    public @Nullable String getFID() {
        if(FID == null)
            FID = getNBT().getString("FID");

        return FID;
    }

    public void setFID(@Nullable String FID) {
        this.FID = FID;

        NBTItem nbtItem = getNBT();

        if(FID == null) nbtItem.removeKey("FID");
        else nbtItem.setString("FID", FID);

        nbtItem.applyNBT(this);
    }

    public @Nullable Position getPosition() {
        return position;
    }

    public boolean containsPosition() {
        return position != null;
    }

    public ItemWrapper setPosition(@Nullable Position position) {
        this.position = position;

        return this;
    }

    public @NotNull NBTItem getNBT() {
        return new NBTItem(this);
    }

    public static ItemWrapper of(@NotNull ItemStack spigotItem) {
        ItemWrapper centreItem = new ItemWrapper(spigotItem.getType());

        centreItem.setAmount(spigotItem.getAmount());
        centreItem.setData(spigotItem.getData());
        centreItem.setItemMeta(spigotItem.getItemMeta());

        return centreItem;
    }

}
