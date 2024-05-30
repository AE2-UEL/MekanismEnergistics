package com.mekeng.github.util.helpers;

import appeng.items.contents.CellConfig;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.item.ItemDummyGas;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class GasCellConfig extends CellConfig {
    public GasCellConfig(ItemStack is) {
        super(is);
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || stack.getItem() == ItemAndBlocks.DUMMY_GAS) {
            super.insertItem(slot, stack, simulate);
        }
        GasStack gas = Utils.getGasFromItem(stack);
        if (gas == null) {
            return stack;
        }
        gas.amount = 1000;
        ItemStack is = new ItemStack(ItemAndBlocks.DUMMY_GAS);
        ItemDummyGas item = (ItemDummyGas) is.getItem();
        item.setGasStack(is, gas);
        return super.insertItem(slot, is, simulate);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == ItemAndBlocks.DUMMY_GAS) {
            super.setStackInSlot(slot, stack);
        }
        GasStack gas = Utils.getGasFromItem(stack);
        if (gas == null) {
            return;
        }

        gas.amount = 1000;
        ItemStack is = new ItemStack(ItemAndBlocks.DUMMY_GAS);
        ItemDummyGas item = (ItemDummyGas) is.getItem();
        item.setGasStack(is, gas);
        super.setStackInSlot(slot, is);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == ItemAndBlocks.DUMMY_GAS) {
            super.isItemValid(slot, stack);
        }
        GasStack gas = Utils.getGasFromItem(stack);
        if (gas == null) {
            return false;
        }
        gas.amount = 1000;
        ItemStack is = new ItemStack(ItemAndBlocks.DUMMY_GAS);
        ItemDummyGas item = (ItemDummyGas) is.getItem();
        item.setGasStack(is, gas);
        return super.isItemValid(slot, is);
    }

}
