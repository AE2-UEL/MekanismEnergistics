package com.mekeng.github.common.me.client;

import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nonnull;

public class ClientDCInternalGasInv implements Comparable<ClientDCInternalGasInv> {

    private final String unlocalizedName;
    private final IGasInventory inventory;

    private final long id;
    private final long sortBy;

    public ClientDCInternalGasInv(final int size, final long id, final long sortBy, final String unlocalizedName) {
        this.inventory = new GasInventory(size, 1);
        this.unlocalizedName = unlocalizedName;
        this.id = id;
        this.sortBy = sortBy;
    }

    public ClientDCInternalGasInv(final int size, final long id, final long sortBy, final String unlocalizedName, int stackSize) {
        this.inventory = new GasInventory(size, stackSize);
        this.unlocalizedName = unlocalizedName;
        this.id = id;
        this.sortBy = sortBy;
    }

    public String getName() {
        final String s = I18n.translateToLocal(this.unlocalizedName + ".name");
        if (s.equals(this.unlocalizedName + ".name")) {
            return I18n.translateToLocal(this.unlocalizedName);
        }
        return s;
    }

    @Override
    public int compareTo(@Nonnull final ClientDCInternalGasInv o) {
        return Long.compare(this.sortBy, o.sortBy);
    }

    public IGasInventory getInventory() {
        return this.inventory;
    }

    public long getId() {
        return this.id;
    }
}
