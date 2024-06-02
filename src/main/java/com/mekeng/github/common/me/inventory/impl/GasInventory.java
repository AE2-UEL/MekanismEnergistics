package com.mekeng.github.common.me.inventory.impl;

import appeng.util.Platform;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Iterator;

public class GasInventory implements IGasInventory {

    private final GasTank[] tanks;

    public GasInventory(int size, int cap, @Nullable IGasInventoryHost host) {
        final IGasInventoryHost h = host == null ? IGasInventoryHost.empty() : host;
        this.tanks = new GasTank[size];
        for (int i = 0; i < size; i ++) {
            final int index = i;
            this.tanks[i] = new NotifiableGasTank(cap,() -> h.onGasInventoryChanged(this, index));
        }
    }

    public GasInventory(int size, @Nullable IGasInventoryHost host) {
        this(size, Integer.MAX_VALUE, host);
    }

    public GasInventory(int size, int cap) {
        this(size, cap, null);
    }

    public GasInventory(int size) {
        this(size, Integer.MAX_VALUE, null);
    }

    public NBTTagCompound save() {
        NBTTagCompound data = new NBTTagCompound();
        for (int i = 0; i < this.tanks.length; i ++) {
            if (this.tanks[i] != null) {
                data.setTag("#" + i, this.tanks[i].write(new NBTTagCompound()));
            }
        }
        return data;
    }

    public void load(NBTTagCompound data) {
        for (int i = 0; i < this.tanks.length; i ++) {
            if (data.hasKey("#" + i, 10)) {
                GasTank tank = GasTank.readFromNBT(data.getCompoundTag("#" + i));
                if (tank != null) {
                    this.tanks[i].setMaxGas(tank.getMaxGas());
                    this.tanks[i].setGas(tank.getGas());
                }
            }
        }
    }

    @Override
    public int size() {
        return this.tanks.length;
    }

    @Override
    public boolean usable(int index) {
        return this.tanks[index] != null && this.tanks[index].getMaxGas() > 0;
    }

    @Override
    public GasTank[] getTanks() {
        return this.tanks;
    }

    @Override
    public GasStack getGasStack(int index) {
        if (this.usable(index)) {
            return this.tanks[index].getGas();
        }
        return null;
    }

    @Override
    public int addGas(int index, @Nonnull GasStack stack, boolean simulate) {
        if (this.usable(index)) {
            return this.tanks[index].receive(stack, !simulate);
        }
        return 0;
    }

    @Override
    public GasStack removeGas(int index, @Nonnull GasStack stack, boolean simulate) {
        if (this.usable(index) && this.tanks[index].canDraw(stack.getGas())) {
            return this.tanks[index].draw(stack.amount, !simulate);
        }
        return null;
    }

    @Override
    public GasStack removeGas(int index, int amount, boolean simulate) {
        if (this.usable(index)) {
            return this.tanks[index].draw(amount, !simulate);
        }
        return null;
    }

    @Override
    public void setGas(int index, @Nullable GasStack stack) {
        if (this.usable(index)) {
            this.tanks[index].setGas(stack);
        }
    }

    @Override
    public void setCap(int cap) {
        for (GasTank tank : this.tanks) {
            if (tank != null) {
                tank.setMaxGas(cap);
            }
        }
    }

    @Nonnull
    @Override
    public Iterator<GasTank> iterator() {
        return Arrays.stream(this.tanks).iterator();
    }

    private static class NotifiableGasTank extends GasTank {

        private final Runnable callback;

        NotifiableGasTank(int max, Runnable callback) {
            super(max);
            this.callback = callback;
        }

        @Override
        public GasStack draw(int amount, boolean doDraw) {
            GasStack ret = super.draw(amount, doDraw);
            if (doDraw && Platform.isServer()) {
                this.callback.run();
            }
            return ret;
        }

        @Override
        public int receive(GasStack amount, boolean doReceive) {
            int ret = super.receive(amount, doReceive);
            if (doReceive && Platform.isServer()) {
                this.callback.run();
            }
            return ret;
        }

        @Override
        public void setMaxGas(int capacity) {
            super.setMaxGas(capacity);
            if (Platform.isServer()) {
                this.callback.run();
            }
        }

        @Override
        public void setGas(GasStack stack) {
            super.setGas(stack);
            if (Platform.isServer()) {
                this.callback.run();
            }
        }

    }

}
