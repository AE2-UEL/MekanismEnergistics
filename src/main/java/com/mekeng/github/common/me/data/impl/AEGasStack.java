package com.mekeng.github.common.me.data.impl;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IStorageChannel;
import appeng.util.item.AEStack;
import com.google.common.primitives.Ints;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AEGasStack extends AEStack<IAEGasStack> implements IAEGasStack, Comparable<AEGasStack> {

    private final Gas gas;

    private AEGasStack(@Nonnull Gas gas, long amt) {
        this.gas = gas;
        this.setStackSize(amt);
        this.setCraftable(false);
        this.setCountRequestable(0L);
    }

    private AEGasStack(GasStack gasStack) {
        this(gasStack.getGas(), gasStack.amount);
    }

    private AEGasStack(AEGasStack gasStack) {
        this.gas = gasStack.gas;
        this.setStackSize(gasStack.getStackSize());
        this.setCraftable(gasStack.isCraftable());
        this.setCountRequestable(gasStack.getCountRequestable());
    }

    @Nullable
    public static AEGasStack of(GasStack input) {
        return input == null || input.getGas() == null ? null : new AEGasStack(input);
    }

    @Nullable
    public static IAEGasStack of(NBTTagCompound data) {
        GasStack gasStack = GasStack.readFromNBT(data);
        if (gasStack == null) {
            return null;
        } else {
            return of(gasStack)
                    .setStackSize(data.getLong("Cnt"))
                    .setCountRequestable(data.getLong("Req"))
                    .setCraftable(data.getBoolean("Craft"));
        }
    }

    @Nullable
    public static IAEGasStack of(ByteBuf buffer) {
        Gas gas = GasRegistry.getGas(buffer.readShort());
        long amt = buffer.readLong();
        if (gas != null) {
            return new AEGasStack(gas, amt)
                    .setCountRequestable(buffer.readLong())
                    .setCraftable(buffer.readBoolean());
        }
        return null;
    }

    @Override
    public GasStack getGasStack() {
        return new GasStack(this.gas, Ints.saturatedCast(this.getStackSize()));
    }

    @Override
    public Gas getGas() {
        return this.gas;
    }

    @Override
    public void add(IAEGasStack option) {
        if (option != null) {
            this.incStackSize(option.getStackSize());
            this.setCountRequestable(this.getCountRequestable() + option.getCountRequestable())
                    .setCraftable(this.isCraftable() || option.isCraftable());
        }
    }

    @Override
    protected boolean hasTagCompound() {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        GasStack stack = this.getGasStack();
        stack.write(nbt);
        nbt.setLong("Cnt", this.getStackSize());
        nbt.setLong("Req", this.getCountRequestable());
        nbt.setBoolean("Craft", this.isCraftable());
    }

    @Override
    public boolean fuzzyComparison(IAEGasStack gasStack, FuzzyMode fuzzyMode) {
        return this.gas == gasStack.getGas();
    }

    @Override
    public void writeToPacket(ByteBuf buffer) {
        buffer.writeShort(this.gas.getID());
        buffer.writeLong(this.getStackSize());
        buffer.writeLong(this.getCountRequestable());
        buffer.writeBoolean(this.isCraftable());
    }

    @Override
    public IAEGasStack copy() {
        return new AEGasStack(this);
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public ItemStack asItemStackRepresentation() {
        ItemStack stack = new ItemStack(ItemAndBlocks.DUMMY_GAS);
        ItemAndBlocks.DUMMY_GAS.setGasStack(stack, this.getGasStack());
        return stack;
    }

    @Override
    public int compareTo(AEGasStack o) {
        if (o.gas != this.gas) {
            return o.gas.getName().compareTo(this.gas.getName());
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return this.gas.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof IAEGasStack) {
            return ((IAEGasStack) other).getGas() == this.gas;
        } else if (other instanceof GasStack) {
            return ((GasStack) other).getGas() == this.gas;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getStackSize() + "x" + this.gas.getName();
    }

}
