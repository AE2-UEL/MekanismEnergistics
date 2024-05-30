package com.mekeng.github.util.helpers;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.container.sync.IGasSyncContainer;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import com.mekeng.github.network.packet.SGasSlotSync;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class GasSyncHelper {

    private final IGasInventory inv;
    private final IGasInventory cache;
    private final int idOffset;

    public static GasSyncHelper create(final IGasInventory inv, final int idOffset) {
        return new GasSyncHelper(inv, idOffset);
    }

    private GasSyncHelper(final IGasInventory inv, final int idOffset) {
        this.inv = inv;
        this.cache = new GasInventory(inv.size());
        this.idOffset = idOffset;
    }

    public void sendFull(final Iterable<IContainerListener> listeners) {
        this.sendDiffMap(this.createDiffMap(true), listeners);
    }

    public void sendDiff(final Iterable<IContainerListener> listeners) {
        this.sendDiffMap(this.createDiffMap(false), listeners);
    }

    public void readPacket(final Map<Integer, IAEGasStack> data) {
        for (int i = 0; i < this.inv.size(); ++i) {
            if (data.containsKey(i + this.idOffset)) {
                this.inv.setGas(i, data.get(i + this.idOffset) == null ? null : data.get(i + this.idOffset).getGasStack());
            }
        }
    }

    private void sendDiffMap(final Map<Integer, IAEGasStack> data, final Iterable<IContainerListener> listeners) {
        if (data.isEmpty()) {
            return;
        }
        for (final IContainerListener l : listeners) {
            if (l instanceof EntityPlayerMP) {
                Container c = ((EntityPlayerMP) l).openContainer;
                if (c instanceof IGasSyncContainer) {
                    ((IGasSyncContainer) c).receiveGasSlots(data);
                }
                MekEng.proxy.netHandler.sendTo(new SGasSlotSync(data), (EntityPlayerMP) l);
            }
        }
    }

    private Map<Integer, IAEGasStack> createDiffMap(final boolean full) {
        final Map<Integer, IAEGasStack> ret = new HashMap<>();
        for (int i = 0; i < this.inv.size(); ++i) {
            if (full || !this.equalsSlot(i)) {
                ret.put(i + this.idOffset, AEGasStack.of(this.inv.getGasStack(i)));
            }
            if (!full) {
                this.cache.setGas(i, this.inv.getGasStack(i));
            }
        }
        return ret;
    }

    private boolean equalsSlot(int slot) {
        final GasStack stackA = this.inv.getGasStack(slot);
        final GasStack stackB = this.cache.getGasStack(slot);

        if (!Objects.equals(stackA, stackB)) {
            return false;
        }
        return stackA == null || stackA.amount == stackB.amount;
    }

}
