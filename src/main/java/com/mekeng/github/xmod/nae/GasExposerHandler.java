package com.mekeng.github.xmod.nae;

import appeng.api.AEApi;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import co.neeve.nae2.common.helpers.exposer.AEStackExposerHandler;
import co.neeve.nae2.common.interfaces.IExposerHost;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IExtendedGasHandler;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nonnull;
import java.util.Optional;

public class GasExposerHandler extends AEStackExposerHandler<IAEGasStack> implements IExtendedGasHandler {

    private static final GasTankInfo[] EMPTY = new GasTankInfo[0];
    private GasTankInfo[] cachedProperties;

    public GasExposerHandler(IExposerHost host) {
        super(host);
    }

    @Override
    protected IStorageChannel<IAEGasStack> getStorageChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public GasStack drawGas(EnumFacing side, GasStack resource, boolean doTransfer) {
        this.updateMonitor();
        IItemList<IAEGasStack> storage = this.getStorageList();
        if (storage != null) {
            AEGasStack aeStack = AEGasStack.of(resource);
            IAEGasStack stack = storage.findPrecise(aeStack);
            if (stack != null) {
                IAEGasStack pulled = this.pullStack(aeStack, !doTransfer);
                if (pulled != null) {
                    return pulled.getGasStack();
                }
            }
        }
        return null;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack stack, boolean doTransfer) {
        return 0;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        this.updateMonitor();
        if (!this.cache.isEmpty()) {
            Optional<IAEGasStack> stack = this.cache.stream().findFirst();
            IAEGasStack pulled = this.pullStack((stack.get()).copy().setStackSize(amount), !doTransfer);
            if (pulled != null) {
                return pulled.getGasStack();
            }
        }
        return null;
    }

    @Override
    protected void refreshCache() {
        super.refreshCache();
        this.rebuildProperties();
    }

    private void rebuildProperties() {
        if (this.cache.isEmpty()) {
            this.cachedProperties = null;
        } else {
            this.cachedProperties = new GasTankInfo[this.cache.size()];
            int i = 0;
            for (IAEGasStack cachedGas : this.cache) {
                GasTank info = new GasTank((int) cachedGas.getStackSize());
                info.setGas(cachedGas.getGasStack());
                this.cachedProperties[i++] = info;
            }
        }
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return false;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return true;
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        this.updateMonitor();
        return this.cachedProperties != null ? this.cachedProperties : EMPTY;
    }

}
