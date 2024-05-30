package com.mekeng.github.common.me.inventory.impl;

import com.mekeng.github.common.me.inventory.IExtendedGasHandler;
import com.mekeng.github.common.me.inventory.IGasInventory;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumSet;

public class GasInvHandler implements IExtendedGasHandler {

    private final IGasInventory inv;
    private final EnumSet<EnumFacing> validSide;

    public GasInvHandler(IGasInventory inv) {
        this(inv, EnumSet.allOf(EnumFacing.class));
    }

    public GasInvHandler(IGasInventory inv, EnumSet<EnumFacing> faces) {
        this.inv = inv;
        this.validSide = faces;
    }

    public void setSide(Collection<EnumFacing> newSides) {
        this.validSide.clear();
        this.validSide.addAll(newSides);
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (this.checkSide(side) && stack != null) {
            GasStack left = stack.copy();
            for (int i = 0; i < this.inv.size(); i ++) {
                if (left.amount <= 0) {
                    break;
                }
                left.amount -= this.inv.addGas(i, left, !doTransfer);
            }
            if (left.amount <= 0) {
                return stack.amount;
            }
            return stack.amount - left.amount;
        }
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        if (this.checkSide(side) && amount > 0) {
            Gas type = null;
            for (int i = 0; i < this.inv.size(); i ++) {
                if (this.inv.getGasStack(i) != null) {
                    type = this.inv.getGasStack(i).getGas();
                    break;
                }
            }
            if (type != null) {
                GasStack toRemove = new GasStack(type, amount);
                for (int i = 0; i < this.inv.size(); i ++) {
                    GasStack tmp = this.inv.removeGas(i, toRemove, !doTransfer);
                    if (tmp != null) {
                        toRemove.amount -= tmp.amount;
                    }
                    if (toRemove.amount <= 0) {
                        break;
                    }
                }
                if (amount - toRemove.amount <= 0) {
                    return null;
                }
                return new GasStack(type, amount - toRemove.amount);
            }
        }
        return null;
    }

    @Override
    public GasStack drawGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        if (this.checkSide(side) && stack != null && stack.amount > 0) {
            GasStack toRemove = stack.copy();
            int amount = stack.amount;
            for (int i = 0; i < this.inv.size(); i ++) {
                GasStack tmp = this.inv.removeGas(i, toRemove, !doTransfer);
                if (tmp != null) {
                    toRemove.amount -= tmp.amount;
                }
                if (toRemove.amount <= 0) {
                    break;
                }
            }
            if (amount - toRemove.amount <= 0) {
                return null;
            }
            return new GasStack(stack.getGas(), amount - toRemove.amount);
        }
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        if (this.checkSide(side)) {
            for (int i = 0; i < this.inv.size(); i ++) {
                if (this.inv.addGas(i, new GasStack(type, 1), true) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        if (this.checkSide(side)) {
            for (int i = 0; i < this.inv.size(); i ++) {
                if (this.inv.removeGas(i, new GasStack(type, 1), true) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        return this.inv.getTanks();
    }

    public boolean checkSide(@Nullable EnumFacing side) {
        return side == null || this.validSide.contains(side);
    }

}
