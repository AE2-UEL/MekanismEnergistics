package com.mekeng.github.common.me.inventory.impl;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.ITickingMonitor;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.part.PartGasStorageBus;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GasHandlerAdapter implements IMEInventory<IAEGasStack>, IBaseMonitor<IAEGasStack>, ITickingMonitor {
    private final Map<IMEMonitorHandlerReceiver<IAEGasStack>, Object> listeners = new HashMap<>();
    private IActionSource source;
    private final IGasHandler gasHandler;
    private final IGridProxyable proxyable;
    private final InventoryCache cache;
    private StorageFilter mode;
    private AccessRestriction access;
    private final EnumFacing face;

    public GasHandlerAdapter(IGasHandler gasHandler, IGridProxyable proxy, EnumFacing face) {
        this.gasHandler = gasHandler;
        this.proxyable = proxy;
        this.face = face;
        if (this.proxyable instanceof PartGasStorageBus) {
            PartGasStorageBus partGasStorageBus = (PartGasStorageBus) this.proxyable;
            this.mode = ((StorageFilter) partGasStorageBus.getConfigManager().getSetting(Settings.STORAGE_FILTER));
            this.access = ((AccessRestriction) partGasStorageBus.getConfigManager().getSetting(Settings.ACCESS));
        }
        this.cache = new InventoryCache(this.gasHandler, this.mode, this.face);
        this.cache.update();
    }

    @Override
    public IAEGasStack injectItems(IAEGasStack input, Actionable type, IActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        GasStack gasStack = input.getGasStack();

        if (!this.gasHandler.canReceiveGas(this.face, gasStack.getGas())) {
            return input;
        }

        // Insert
        int wasFillled = this.gasHandler.receiveGas(this.face, gasStack, type != Actionable.SIMULATE);
        int remaining = gasStack.amount - wasFillled;
        if (gasStack.amount == remaining) {
            // The stack was unmodified, target tank is full
            return input;
        }

        if (type == Actionable.MODULATE) {
            IAEGasStack added = input.copy().setStackSize(input.getStackSize() - remaining);
            this.cache.currentlyCached.add(added);
            this.postDifference(Collections.singletonList(added));
            try {
                this.proxyable.getProxy().getTick().alertDevice(this.proxyable.getProxy().getNode());
            } catch (GridAccessException ex) {
                // meh
            }
        }

        gasStack.amount = remaining;

        return AEGasStack.of(gasStack);
    }

    @Override
    public IAEGasStack extractItems(IAEGasStack request, Actionable mode, IActionSource src) {
        if (request == null || request.getStackSize() == 0) {
            return null;
        }
        GasStack requestedGasStack = request.getGasStack();

        if (!this.gasHandler.canDrawGas(this.face, requestedGasStack.getGas())) {
            return null;
        }

        final boolean doDrain = (mode == Actionable.MODULATE);

        // Drain the gas from the tank
        GasStack gathered = Utils.drawGas(this.gasHandler, requestedGasStack, this.face, requestedGasStack.amount, doDrain);
        if (gathered == null) {
            // If nothing was pulled from the tank, return null
            return null;
        }

        IAEGasStack gatheredAEGasStack = AEGasStack.of(gathered);
        if (mode == Actionable.MODULATE) {
            IAEGasStack cachedStack = this.cache.currentlyCached.findPrecise(request);
            if (cachedStack != null) {
                cachedStack.decStackSize(gatheredAEGasStack.getStackSize());
                this.postDifference(Collections.singletonList(gatheredAEGasStack.copy().setStackSize(-gatheredAEGasStack.getStackSize())));
            }
            try {
                this.proxyable.getProxy().getTick().alertDevice(this.proxyable.getProxy().getNode());
            } catch (GridAccessException ex) {
                // meh
            }
        }
        return gatheredAEGasStack;
    }

    @Override
    public TickRateModulation onTick() {
        List<IAEGasStack> changes = this.cache.update();
        if (!changes.isEmpty() && access.hasPermission(AccessRestriction.READ)) {
            this.postDifference(changes);
            return TickRateModulation.URGENT;
        } else {
            return TickRateModulation.SLOWER;
        }
    }

    @Override
    public IItemList<IAEGasStack> getAvailableItems(IItemList<IAEGasStack> out) {
        return this.cache.getAvailableItems(out);
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public void setActionSource(IActionSource source) {
        this.source = source;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEGasStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEGasStack> l) {
        this.listeners.remove(l);
    }

    private void postDifference(Iterable<IAEGasStack> a) {
        final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEGasStack>, Object>> i = this.listeners.entrySet().iterator();
        while (i.hasNext()) {
            final Map.Entry<IMEMonitorHandlerReceiver<IAEGasStack>, Object> l = i.next();
            final IMEMonitorHandlerReceiver<IAEGasStack> key = l.getKey();
            if (key.isValid(l.getValue())) {
                key.postChange(this, a, this.source);
            } else {
                i.remove();
            }
        }
    }

    private static class InventoryCache {
        private final IGasHandler gasHandler;
        private final StorageFilter mode;
        private final EnumFacing face;
        IItemList<IAEGasStack> currentlyCached = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();

        public InventoryCache(IGasHandler gasHandler, StorageFilter mode, EnumFacing face) {
            this.mode = mode;
            this.gasHandler = gasHandler;
            this.face = face;
        }

        public List<IAEGasStack> update() {
            final List<IAEGasStack> changes = new ArrayList<>();
            final GasTankInfo[] tankProperties = this.gasHandler.getTankInfo();

            IItemList<IAEGasStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();

            for (GasTankInfo tankProperty : tankProperties) {
                if (this.mode == StorageFilter.EXTRACTABLE_ONLY && this.gasHandler.drawGas(this.face, 1, false) == null) {
                    continue;
                }
                currentlyOnStorage.add(AEGasStack.of(tankProperty.getGas()));
            }

            for (final IAEGasStack is : currentlyCached) {
                is.setStackSize(-is.getStackSize());
            }

            for (final IAEGasStack is : currentlyOnStorage) {
                currentlyCached.add(is);
            }

            for (final IAEGasStack is : currentlyCached) {
                if (is.getStackSize() != 0) {
                    changes.add(is);
                }
            }

            currentlyCached = currentlyOnStorage;

            return changes;
        }

        public IItemList<IAEGasStack> getAvailableItems(IItemList<IAEGasStack> out) {
            currentlyCached.iterator().forEachRemaining(out::add);
            return out;
        }

    }
}
