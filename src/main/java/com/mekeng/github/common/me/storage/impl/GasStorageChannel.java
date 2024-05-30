package com.mekeng.github.common.me.storage.impl;

import appeng.api.storage.data.IItemList;
import com.google.common.base.Preconditions;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Utils;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GasStorageChannel implements IGasStorageChannel {

    public static GasStorageChannel INSTANCE = new GasStorageChannel();

    private GasStorageChannel() {
        // NO-OP
    }

    @Override
    public int transferFactor() {
        return 4000;
    }

    @Override
    public int getUnitsPerByte() {
        return 32000;
    }

    @Nonnull
    @Override
    public IItemList<IAEGasStack> createList() {
        return GasList.create();
    }

    @Nullable
    @Override
    public IAEGasStack createStack(@Nonnull Object input) {
        Preconditions.checkNotNull(input);
        if (input instanceof GasStack) {
            return AEGasStack.of((GasStack) input);
        } else if (input instanceof ItemStack) {
            ItemStack is = (ItemStack) input;
            return is.getItem() == ItemAndBlocks.DUMMY_GAS ? AEGasStack.of(ItemAndBlocks.DUMMY_GAS.getGasStack(is)) : AEGasStack.of(Utils.getGasFromItem(is));
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public IAEGasStack readFromPacket(@Nonnull ByteBuf input) {
        Preconditions.checkNotNull(input);
        return AEGasStack.of(input);
    }

    @Nullable
    @Override
    public IAEGasStack createFromNBT(@Nonnull NBTTagCompound nbt) {
        Preconditions.checkNotNull(nbt);
        return AEGasStack.of(nbt);
    }

}
