package com.mekeng.github.util.helpers;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;
import mekanism.common.base.ITierItem;
import mekanism.common.tier.BaseTier;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class ItemGasHandler {

    private final IGasItem handler;
    private final ItemStack stack;

    public ItemGasHandler(IGasItem handler, ItemStack stack) {
        this.handler = handler;
        this.stack = stack;
    }

    public int addGas(@Nullable GasStack gas, boolean add) {
        if (gas == null || !this.handler.canReceiveGas(this.stack, gas.getGas())) {
            return 0;
        }
        int space = this.isCreative() ? Integer.MAX_VALUE : this.capacity() - this.gasAmount();
        int toAdd = Math.min(space, gas.amount);
        if (toAdd <= 0) {
            return 0;
        }
        if (add) {
            int newAmt = isCreative() ? Integer.MAX_VALUE : toAdd + this.gasAmount();
            this.handler.setGas(this.stack, gas.copy().withAmount(newAmt));
        }
        return toAdd;
    }

    @Nullable
    public GasStack removeGas(@Nullable GasStack gas, boolean drain) {
        if (gas == null || !this.handler.canProvideGas(this.stack, gas.getGas())) {
            return null;
        }
        return removeGas(gas.amount, drain);
    }

    public GasStack removeGas(int gas, boolean drain) {
        int left = isCreative() ? Integer.MAX_VALUE : this.gasAmount();
        int toRemove = Math.min(gas, left);
        if (toRemove <= 0 || this.handler.getGas(this.stack) == null) {
            return null;
        }
        GasStack removed = this.handler.getGas(this.stack).copy().withAmount(toRemove);
        if (drain && !isCreative()) {
            GasStack leftGas = removed.copy().withAmount(left - toRemove);
            if (leftGas.amount == 0) {
                leftGas = null;
            }
            this.handler.setGas(this.stack, leftGas);
        }
        return removed;
    }

    public int gasAmount() {
        GasStack gas = this.handler.getGas(this.stack);
        return gas == null ? 0 : gas.amount;
    }

    public int capacity() {
        return this.handler.getMaxGas(this.stack);
    }

    public ItemStack getContainer() {
        return this.stack;
    }

    private boolean isCreative() {
        if (this.stack.getItem() instanceof ITierItem) {
            return ((ITierItem) this.stack.getItem()).getBaseTier(this.stack) == BaseTier.CREATIVE;
        }
        return false;
    }

}
