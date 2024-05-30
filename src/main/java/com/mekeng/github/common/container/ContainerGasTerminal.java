package com.mekeng.github.common.container;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.parts.IPart;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotPlayerHotBar;
import appeng.container.slot.SlotPlayerInv;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.InventoryAction;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.util.ConfigManager;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.me.storage.IPortableGasCell;
import com.mekeng.github.network.packet.SMEGasInventoryUpdate;
import com.mekeng.github.network.packet.sync.IActionHolder;
import com.mekeng.github.network.packet.sync.Paras;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.ItemGasHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class ContainerGasTerminal extends AEBaseContainer implements IConfigManagerHost, IConfigurableObject, IMEMonitorHandlerReceiver<IAEGasStack>, IActionHolder {
    private final IConfigManager clientCM;
    private final IMEMonitor<IAEGasStack> monitor;
    private final IItemList<IAEGasStack> gases = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();
    @GuiSync(99)
    public boolean hasPower = false;
    private final ITerminalHost terminal;
    private IConfigManager serverCM;
    private IConfigManagerHost gui;
    private IGridNode networkNode;
    // Holds the gas the client wishes to extract, or null for insert
    private IAEGasStack clientRequestedTargetGas = null;
    private final Map<String, Consumer<Paras>> holder = createHolder();

    public ContainerGasTerminal(final InventoryPlayer ip, final ITerminalHost monitorable) {
        this(ip, monitorable, true);
    }

    public ContainerGasTerminal(final InventoryPlayer ip, final ITerminalHost monitorable, final boolean bindInventory) {
        this(ip, monitorable, null, bindInventory);
    }

    public ContainerGasTerminal(InventoryPlayer ip, ITerminalHost terminal, IGuiItemObject guiObj, boolean bindInventory) {
        super(ip, terminal instanceof TileEntity ? (TileEntity) terminal : null, terminal instanceof IPart ? (IPart) terminal : null, guiObj);
        this.terminal = terminal;
        this.clientCM = new ConfigManager(this);

        this.clientCM.registerSetting(Settings.SORT_BY, SortOrder.NAME);
        this.clientCM.registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING);
        this.clientCM.registerSetting(Settings.VIEW_MODE, ViewItems.ALL);
        this.holder.put("set_target", o -> this.setTargetGasStack(o.get(0)));
        if (Platform.isServer()) {
            this.serverCM = terminal.getConfigManager();
            this.monitor = terminal.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            if (this.monitor != null) {
                this.monitor.addListener(this, null);
                if (terminal instanceof IPortableGasCell) {
                    this.setPowerSource((IEnergySource) terminal);
                    if (terminal instanceof WirelessTerminalGuiObject) {
                        this.networkNode = ((WirelessTerminalGuiObject) terminal).getActionableNode();
                    }
                } else if (terminal instanceof IEnergySource) {
                    this.setPowerSource((IEnergySource) terminal);
                } else if (terminal instanceof IGridHost || terminal instanceof IActionHost) {
                    final IGridNode node;
                    if (terminal instanceof IGridHost) {
                        node = ((IGridHost) terminal).getGridNode(AEPartLocation.INTERNAL);
                    } else {
                        node = ((IActionHost) terminal).getActionableNode();
                    }
                    if (node != null) {
                        this.networkNode = node;
                        final IGrid g = node.getGrid();
                        this.setPowerSource(new ChannelPowerSrc(this.networkNode, g.getCache(IEnergyGrid.class)));
                    }
                }
            }
        } else {
            this.monitor = null;
        }
        if (bindInventory) {
            this.bindPlayerInventory(ip, 0, 222 - 82);
        }
    }

    @Override
    public boolean isValid(Object verificationToken) {
        return true;
    }

    @Override
    public void postChange(IBaseMonitor<IAEGasStack> monitor, Iterable<IAEGasStack> change, IActionSource actionSource) {
        for (final IAEGasStack is : change) {
            this.gases.add(is);
        }
    }

    @Override
    public void onListUpdate() {
        for (final IContainerListener c : this.listeners) {
            this.queueInventory(c);
        }
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.queueInventory(listener);
    }

    @Override
    public void onContainerClosed(@Nonnull final EntityPlayer player) {
        super.onContainerClosed(player);
        if (this.monitor != null) {
            this.monitor.removeListener(this);
        }
    }

    private void queueInventory(final IContainerListener c) {
        if (Platform.isServer() && c instanceof EntityPlayer && this.monitor != null) {
            SMEGasInventoryUpdate packet = new SMEGasInventoryUpdate();
            final IItemList<IAEGasStack> monitorCache = this.monitor.getStorageList();
            for (final IAEGasStack send : monitorCache) {
                if (packet.needFlush()) {
                    MekEng.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                    packet.clear();
                }
                packet.addGas(send);
            }
            if (!packet.isEmpty()) {
                MekEng.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
            }
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        if (Platform.isServer()) {
            return this.serverCM;
        }
        return this.clientCM;
    }

    public void setTargetGasStack(final IAEGasStack stack) {
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetGas == null) {
                return;
            }
            if (stack != null && this.clientRequestedTargetGas != null
                    && stack.getGas() == this.clientRequestedTargetGas.getGas()) {
                return;
            }
        }
        this.clientRequestedTargetGas = stack == null ? null : stack.copy();
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isServer()) {
            if (this.monitor != this.terminal.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class))) {
                this.setValidContainer(false);
            }
            for (final Settings set : this.serverCM.getSettings()) {
                final Enum<?> sideLocal = this.serverCM.getSetting(set);
                final Enum<?> sideRemote = this.clientCM.getSetting(set);

                if (sideLocal != sideRemote) {
                    this.clientCM.putSetting(set, sideLocal);
                    for (final IContainerListener crafter : this.listeners) {
                        if (crafter instanceof EntityPlayerMP) {
                            try {
                                NetworkHandler.instance().sendTo(new PacketValueConfig(set.name(), sideLocal.name()), (EntityPlayerMP) crafter);
                            } catch (final IOException e) {
                                AELog.debug(e);
                            }
                        }
                    }
                }
            }

            if (!this.gases.isEmpty()) {
                final IItemList<IAEGasStack> monitorCache = this.monitor.getStorageList();
                final SMEGasInventoryUpdate packet = new SMEGasInventoryUpdate();
                boolean noUpdate = true;
                for (final IAEGasStack is : this.gases) {
                    if (packet.needFlush()) {
                        for (final Object c : this.listeners) {
                            if (c instanceof EntityPlayer) {
                                MekEng.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                            }
                        }
                        noUpdate = false;
                        packet.clear();
                    }
                    final IAEGasStack send = monitorCache.findPrecise(is);
                    if (send == null) {
                        is.setStackSize(0);
                        packet.addGas(is);
                    } else {
                        packet.addGas(send);
                    }
                }
                if (!packet.isEmpty()) {
                    for (final Object c : this.listeners) {
                        if (c instanceof EntityPlayer) {
                            MekEng.proxy.netHandler.sendTo(packet, (EntityPlayerMP) c);
                        }
                    }
                    noUpdate = false;
                }
                if (!noUpdate) {
                    this.gases.resetStatus();
                }
            }
            this.updatePowerStatus();
            super.detectAndSendChanges();
        }
    }

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx) {
        if (Platform.isClient()) {
            return ItemStack.EMPTY;
        }
        EntityPlayerMP player = (EntityPlayerMP) p;
        if (this.inventorySlots.get(idx) instanceof SlotPlayerInv || this.inventorySlots.get(idx) instanceof SlotPlayerHotBar) {
            final AppEngSlot clickSlot = (AppEngSlot) this.inventorySlots.get(idx); // require AE SLots!
            ItemStack itemStack = clickSlot.getStack();

            ItemStack copy = itemStack.copy();
            copy.setCount(1);
            ItemGasHandler gh = Utils.getItemGasHandler(copy);
            if (gh == null) {
                // only gas handlers items
                return ItemStack.EMPTY;
            }

            int heldAmount = itemStack.getCount();
            for (int i = 0; i < heldAmount; i++) {
                copy = itemStack.copy();
                copy.setCount(1);
                gh = Utils.getItemGasHandler(copy);

                final GasStack extract = gh.removeGas(Integer.MAX_VALUE, false);
                if (extract == null || extract.amount < 1) {
                    return ItemStack.EMPTY;
                }

                final IAEGasStack notStorable = Platform.poweredInsert(this.getPowerSource(), this.monitor, AEGasStack.of(extract), this.getActionSource(), Actionable.SIMULATE);

                if (notStorable != null && notStorable.getStackSize() > 0) {
                    final int toStore = (int) (extract.amount - notStorable.getStackSize());
                    final GasStack storable = gh.removeGas(toStore, false);
                    if (storable == null || storable.amount == 0) {
                        return ItemStack.EMPTY;
                    } else {
                        extract.amount = storable.amount;
                    }
                }

                // Actually drain
                final GasStack drained = gh.removeGas(extract, true);
                extract.amount = drained == null ? 0 : drained.amount;
                final IAEGasStack notInserted = Platform.poweredInsert(this.getPowerSource(), this.monitor, AEGasStack.of(extract), this.getActionSource());
                if (notInserted != null && notInserted.getStackSize() > 0) {
                    IAEGasStack spill = this.monitor.injectItems(notInserted, Actionable.MODULATE, this.getActionSource());
                    if (spill != null && spill.getStackSize() > 0) {
                        gh.addGas(spill.getGasStack(), true);
                    }
                }

                if (notInserted == null || notInserted.getStackSize() == 0) {
                    if (!player.inventory.addItemStackToInventory(gh.getContainer())) {
                        player.dropItem(gh.getContainer(), false);
                    }
                    clickSlot.decrStackSize(1);
                }
            }
            this.detectAndSendChanges();
            return ItemStack.EMPTY;
        }
        return super.transferStackInSlot(p, idx);
    }

    @Override
    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        ItemStack heldCopy = held.copy();
        heldCopy.setCount(1);
        ItemGasHandler gh = Utils.getItemGasHandler(heldCopy);
        if (gh == null) {
            // only gas handlers items
            return;
        }

        if (action == InventoryAction.FILL_ITEM && this.clientRequestedTargetGas != null) {
            final IAEGasStack stack = this.clientRequestedTargetGas.copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = gh.addGas(stack.getGasStack(), false);
            int heldAmount = held.getCount();

            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedGasContainer = held.copy();
                copiedGasContainer.setCount(1);
                gh = Utils.getItemGasHandler(copiedGasContainer);

                // Check if we can pull out of the system
                final IAEGasStack canPull = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack.setStackSize(amountAllowed), this.getActionSource(), Actionable.SIMULATE);
                if (canPull == null || canPull.getStackSize() < 1) {
                    return;
                }

                // How much could fit into the container
                final int canFill = gh.addGas(canPull.getGasStack(), false);

                if (canFill == 0) {
                    return;
                }

                // Now actually pull out of the system
                final IAEGasStack pulled = Platform.poweredExtraction(this.getPowerSource(), this.monitor, stack.setStackSize(canFill), this.getActionSource());
                if (pulled == null || pulled.getStackSize() < 1) {
                    // Something went wrong
                    MekEng.log.error("Unable to pull gas out of the ME system even though the simulation said yes ");
                    return;
                }

                // Actually fill
                final int used = gh.addGas(pulled.getGasStack(), true);

                if (used != canFill) {
                    MekEng.log.error("Gas item [%s] reported a different possible amount than it actually accepted.", held.getDisplayName());
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(gh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(gh.getContainer())) {
                        player.dropItem(gh.getContainer(), false);
                    }
                }
            }
            this.updateHeld(player);
        } else if (action == InventoryAction.EMPTY_ITEM) {
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedGasContainer = held.copy();
                copiedGasContainer.setCount(1);
                gh = Utils.getItemGasHandler(copiedGasContainer);

                // See how much we can drain from the item
                final GasStack extract = gh.removeGas(Integer.MAX_VALUE, false);
                if (extract == null || extract.amount < 1) {
                    return;
                }

                // Check if we can push into the system
                final IAEGasStack notStorable = Platform.poweredInsert(this.getPowerSource(), this.monitor, AEGasStack.of(extract), this.getActionSource(), Actionable.SIMULATE);

                if (notStorable != null && notStorable.getStackSize() > 0) {
                    final int toStore = (int) (extract.amount - notStorable.getStackSize());
                    final GasStack storable = gh.removeGas(toStore, false);
                    if (storable == null || storable.amount == 0) {
                        return;
                    } else {
                        extract.amount = storable.amount;
                    }
                }

                // Actually drain
                final GasStack drained = gh.removeGas(extract, true);
                extract.amount = drained == null ? 0 : drained.amount;

                final IAEGasStack notInserted = Platform.poweredInsert(this.getPowerSource(), this.monitor, AEGasStack.of(extract), this.getActionSource());

                if (notInserted != null && notInserted.getStackSize() > 0) {
                    IAEGasStack spill = this.monitor.injectItems(notInserted, Actionable.MODULATE, this.getActionSource());
                    if (spill != null && spill.getStackSize() > 0) {
                        gh.addGas(spill.getGasStack(), true);
                    }
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(gh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(gh.getContainer())) {
                        player.dropItem(gh.getContainer(), false);
                    }
                }
            }
            this.updateHeld(player);
        }
    }

    protected void updatePowerStatus() {
        try {
            if (this.networkNode != null) {
                this.setPowered(this.networkNode.isActive());
            } else if (this.getPowerSource() instanceof IEnergyGrid) {
                this.setPowered(((IEnergyGrid) this.getPowerSource()).isNetworkPowered());
            } else {
                this.setPowered(this.getPowerSource().extractAEPower(1, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 0.8);
            }
        } catch (final Exception ignore) {
            // :P
        }
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    public boolean isPowered() {
        return this.hasPower;
    }

    private void setPowered(final boolean isPowered) {
        this.hasPower = isPowered;
    }

    @Nonnull
    @Override
    public Map<String, Consumer<Paras>> getActionMap() {
        return this.holder;
    }

}