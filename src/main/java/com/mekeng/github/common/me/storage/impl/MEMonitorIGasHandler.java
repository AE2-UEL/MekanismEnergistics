package com.mekeng.github.common.me.storage.impl;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.StorageFilter;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.me.storage.ITickingMonitor;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
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

public class MEMonitorIGasHandler implements IMEMonitor<IAEGasStack>, ITickingMonitor {
    private final IGasHandler handler;
    private final EnumFacing face;
    private IItemList<IAEGasStack> cache = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();
    private final HashMap<IMEMonitorHandlerReceiver<IAEGasStack>, Object> listeners = new HashMap<>();
    private IActionSource mySource;
    private StorageFilter mode = StorageFilter.EXTRACTABLE_ONLY;

    public MEMonitorIGasHandler(final IGasHandler handler, EnumFacing face) {
        this.handler = handler;
        this.face = face;
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEGasStack> l, final Object verificationToken) {
        this.listeners.put(l, verificationToken);
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEGasStack> l) {
        this.listeners.remove(l);
    }

    @Override
    public IAEGasStack injectItems(final IAEGasStack input, final Actionable type, final IActionSource src) {
        if (input == null || input.getStackSize() == 0) {
            return null;
        }
        final int filled = this.handler.receiveGas(this.face, input.getGasStack(), type == Actionable.MODULATE);

        if (filled == 0) {
            return input.copy();
        }

        if (filled == input.getStackSize()) {
            return null;
        }

        final IAEGasStack o = input.copy();
        o.setStackSize(input.getStackSize() - filled);

        if (type == Actionable.MODULATE) {
            IAEGasStack added = o.copy();
            this.cache.add(added);
            this.postDifference(Collections.singletonList(added));
            this.onTick();
        }

        return o;
    }

    @Override
    public IAEGasStack extractItems(final IAEGasStack request, final Actionable type, final IActionSource src) {
        if (request == null || request.getStackSize() == 0) {
            return null;
        }
        final GasStack removed = Utils.drawGas(this.handler, request.getGasStack(), this.face, request.getGasStack().amount, type == Actionable.MODULATE);

        if (removed == null || !removed.isGasEqual(request.getGasStack()) || removed.amount == 0) {
            return null;
        }

        final IAEGasStack o = request.copy();
        o.setStackSize(removed.amount);

        if (type == Actionable.MODULATE) {
            IAEGasStack cachedStack = this.cache.findPrecise(request);
            if (cachedStack != null) {
                cachedStack.decStackSize(o.getStackSize());
                this.postDifference(Collections.singletonList(o.copy().setStackSize(-o.getStackSize())));
            }
        }
        return o;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public TickRateModulation onTick() {
        boolean changed = false;

        final List<IAEGasStack> changes = new ArrayList<>();
        final GasTankInfo[] tankProperties = this.handler.getTankInfo();

        IItemList<IAEGasStack> currentlyOnStorage = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();

        for (GasTankInfo tankProperty : tankProperties) {
            if (this.mode == StorageFilter.EXTRACTABLE_ONLY && this.handler.drawGas(this.face, 1, false) == null) {
                continue;
            }
            currentlyOnStorage.add(AEGasStack.of(tankProperty.getGas()));
        }

        for (final IAEGasStack is : cache) {
            is.setStackSize(-is.getStackSize());
        }

        for (final IAEGasStack is : currentlyOnStorage) {
            cache.add(is);
        }

        for (final IAEGasStack is : cache) {
            if (is.getStackSize() != 0) {
                changes.add(is);
            }
        }

        cache = currentlyOnStorage;

        if (!changes.isEmpty()) {
            this.postDifference(changes);
            changed = true;
        }

        return changed ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
    }

    private void postDifference(final Iterable<IAEGasStack> a) {
        if (a != null) {
            final Iterator<Map.Entry<IMEMonitorHandlerReceiver<IAEGasStack>, Object>> i = this.listeners.entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry<IMEMonitorHandlerReceiver<IAEGasStack>, Object> l = i.next();
                final IMEMonitorHandlerReceiver<IAEGasStack> key = l.getKey();
                if (key.isValid(l.getValue())) {
                    key.postChange(this, a, this.getActionSource());
                } else {
                    i.remove();
                }
            }
        }
    }

    @Override
    public AccessRestriction getAccess() {
        return AccessRestriction.READ_WRITE;
    }

    @Override
    public boolean isPrioritized(final IAEGasStack input) {
        return false;
    }

    @Override
    public boolean canAccept(final IAEGasStack input) {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return true;
    }

    @Override
    public IItemList<IAEGasStack> getAvailableItems(final IItemList<IAEGasStack> out) {
        for (final IAEGasStack fs : cache) {
            out.addStorage(fs);
        }
        return out;
    }

    @Override
    public IItemList<IAEGasStack> getStorageList() {
        return this.cache;
    }

    private StorageFilter getMode() {
        return this.mode;
    }

    public void setMode(final StorageFilter mode) {
        this.mode = mode;
    }

    private IActionSource getActionSource() {
        return this.mySource;
    }

    @Override
    public void setActionSource(final IActionSource mySource) {
        this.mySource = mySource;
    }

}
