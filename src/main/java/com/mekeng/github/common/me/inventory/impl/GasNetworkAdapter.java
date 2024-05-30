package com.mekeng.github.common.me.inventory.impl;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.util.EnumFacing;

import java.util.EnumSet;
import java.util.function.Supplier;

public class GasNetworkAdapter extends GasInvHandler {

    private final Supplier<IStorageGrid> supplier;
    private final IActionSource source;

    public GasNetworkAdapter(Supplier<IStorageGrid> networkSupplier, IActionSource source, IGasInventory inv) {
        super(inv);
        this.supplier = networkSupplier;
        this.source = source;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        IStorageGrid storage = supplier.get();
        if (storage != null) {
            int originAmt = stack.amount;
            IMEInventory<IAEGasStack> dest = storage.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            IAEGasStack overflow = dest.injectItems(AEGasStack.of(stack), doTransfer ? Actionable.MODULATE : Actionable.SIMULATE, this.source);
            if (overflow != null && overflow.getStackSize() == originAmt) {
                return super.receiveGas(side, stack, doTransfer);
            } else if (overflow != null) {
                return (int) (originAmt - overflow.getStackSize());
            } else {
                return originAmt;
            }
        } else {
            return super.receiveGas(side, stack, doTransfer);
        }
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        IStorageGrid storage = supplier.get();
        if (storage != null) {
            IMEInventory<IAEGasStack> dest = storage.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            if (dest.injectItems(AEGasStack.of(new GasStack(type, 1)), Actionable.SIMULATE, this.source) == null) {
                return true;
            }
        }
        return super.canReceiveGas(side, type);
    }

}
