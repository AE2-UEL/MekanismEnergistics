package com.mekeng.github.common.container;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.util.Platform;
import com.mekeng.github.common.container.sync.IGasSyncContainer;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.network.packet.sync.IActionHolder;
import com.mekeng.github.network.packet.sync.Paras;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.GasSyncHelper;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ContainerGasConfigurable<T extends IUpgradeableHost> extends ContainerUpgradeable implements IGasSyncContainer, IActionHolder {
    protected final GasSyncHelper sync = GasSyncHelper.create(this.getGasConfigInventory(), 0);
    protected final Map<String, Consumer<Paras>> holder = createHolder();

    public ContainerGasConfigurable(InventoryPlayer ip, T te) {
        super(ip, te);
        this.holder.put("jei_set", o -> {
            AEGasStack gas = o.get(1);
            if (gas != null) {
                gas.setStackSize(1000);
                this.getGasConfigInventory().setGas(o.get(0), gas.getGasStack());
                this.receiveGasSlots(Collections.singletonMap(o.get(0), gas));
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T getUpgradeable() {
        return (T) super.getUpgradeable();
    }

    public abstract IGasInventory getGasConfigInventory();

    @Override
    protected ItemStack transferStackToContainer(ItemStack input) {
        GasStack gs = Utils.getGasFromItem(input);
        if (gs != null) {
            final IGasInventory t = this.getGasConfigInventory();
            final IAEGasStack stack = AEGasStack.of(gs);
            for (int i = 0; i < t.size(); ++i) {
                if (t.getGasStack(i) == null && this.isValidForConfig(i, stack)) {
                    t.setGas(i, stack == null ? null : stack.getGasStack());
                    break;
                }
            }
        }
        return input;
    }

    protected boolean isValidForConfig(int slot, @Nullable IAEGasStack gs) {
        if (this.supportCapacity()) {
            // assumes 4 slots per upgrade
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
            if (slot > 0 && upgrades < 1) {
                return false;
            }
            return slot <= 4 || upgrades >= 2;
        }
        return true;
    }

    @Override
    protected void standardDetectAndSendChanges() {
        if (Platform.isServer()) {
            this.sync.sendDiff(this.listeners);
            // clear out config items that are no longer valid (eg capacity upgrade removed)
            final IGasInventory t = this.getGasConfigInventory();
            for (int i = 0; i < t.size(); ++i) {
                if (t.getGasStack(i) != null && !this.isValidForConfig(i, AEGasStack.of(t.getGasStack(i)))) {
                    t.setGas(i, null);
                }
            }
        }
        super.standardDetectAndSendChanges();
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.sync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveGasSlots(Map<Integer, IAEGasStack> gases) {
        this.sync.readPacket(gases);
    }

    @Nonnull
    @Override
    public Map<String, Consumer<Paras>> getActionMap() {
        return this.holder;
    }

}
