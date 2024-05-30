package com.mekeng.github.util.helpers;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.me.helpers.MEMonitorHandler;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.me.storage.IPortableGasCell;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.util.Collections;

public class PortableGasCellViewer extends MEMonitorHandler<IAEGasStack> implements IPortableGasCell, IInventorySlotAware {

    private final ItemStack target;
    private final IAEItemPowerStorage ips;
    private final int inventorySlot;

    public PortableGasCellViewer(final ItemStack is, final int slot) {
        super(AEApi.instance().registries().cell().getCellInventory(is, null, AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)));
        this.ips = (IAEItemPowerStorage) is.getItem();
        this.target = is;
        this.inventorySlot = slot;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public ItemStack getItemStack() {
        return this.target;
    }

    @Override
    public double extractAEPower(double amt, @Nonnull final Actionable mode, final PowerMultiplier usePowerMultiplier) {
        amt = usePowerMultiplier.multiply(amt);

        if (mode == Actionable.SIMULATE) {
            return usePowerMultiplier.divide(Math.min(amt, this.ips.getAECurrentPower(this.target)));
        }

        return usePowerMultiplier.divide(this.ips.extractAEPower(this.target, amt, Actionable.MODULATE));
    }

    @Override
    public IAEGasStack injectItems(IAEGasStack input, Actionable mode, IActionSource src) {
        final long size = input.getStackSize();

        final IAEGasStack injected = super.injectItems(input, mode, src);

        if (mode == Actionable.MODULATE && (injected == null || injected.getStackSize() != size)) {
            this.notifyListenersOfChange(Collections.singletonList(input.copy().setStackSize(input.getStackSize() - (injected == null ? 0 : injected.getStackSize()))), null);
        }

        return injected;
    }

    @Override
    public IAEGasStack extractItems(IAEGasStack request, Actionable mode, IActionSource src) {
        final IAEGasStack extractable = super.extractItems(request, mode, src);

        if (mode == Actionable.MODULATE && extractable != null) {
            this.notifyListenersOfChange(Collections.singletonList(request.copy().setStackSize(-extractable.getStackSize())), null);
        }

        return extractable;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)) {
            return (IMEMonitor<T>) this;
        }
        return null;
    }

    @Override
    public IConfigManager getConfigManager() {
        final ConfigManager out = new ConfigManager((manager, settingName, newValue) ->
        {
            final NBTTagCompound data = Platform.openNbtData(PortableGasCellViewer.this.target);
            manager.writeToNBT(data);
        });

        out.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        out.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        out.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);

        out.readFromNBT(Platform.openNbtData(this.target).copy());
        return out;
    }
}
