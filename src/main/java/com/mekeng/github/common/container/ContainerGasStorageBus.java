package com.mekeng.github.common.container;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.data.IItemList;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotRestrictedInput;
import appeng.util.Platform;
import appeng.util.iterators.NullIterator;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.part.PartGasStorageBus;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;

public class ContainerGasStorageBus extends ContainerGasConfigurable<PartGasStorageBus> {

    @GuiSync(3)
    public AccessRestriction rwMode = AccessRestriction.READ_WRITE;

    @GuiSync(4)
    public StorageFilter storageFilter = StorageFilter.EXTRACTABLE_ONLY;

    public ContainerGasStorageBus(InventoryPlayer ip, PartGasStorageBus te) {
        super(ip, te);
        this.holder.put("clear", o -> this.clear());
        this.holder.put("partition", o -> this.partition());
    }

    @Override
    protected int getHeight() {
        return 251;
    }

    @Override
    protected void setupConfig() {
        final IItemHandler upgrades = this.getUpgradeable().getInventoryByName("upgrades");
        this.addSlotToContainer((new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 0, 187, 8, this.getInventoryPlayer()))
                .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 1, 187, 8 + 18, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 2, 187, 8 + 18 * 2, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 3, 187, 8 + 18 * 3, this.getInventoryPlayer()))
                        .setNotDraggable());
        this.addSlotToContainer(
                (new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.UPGRADES, upgrades, 4, 187, 8 + 18 * 4, this.getInventoryPlayer()))
                        .setNotDraggable());
    }

    @Override
    protected boolean isValidForConfig(int slot, IAEGasStack fs) {
        if (this.supportCapacity()) {
            final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

            final int y = slot / 9;

            return y < upgrades + 2;
        }

        return true;
    }

    @Override
    public int availableUpgrades() {
        return 5;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.setFuzzyMode((FuzzyMode) this.getUpgradeable().getConfigManager().getSetting(Settings.FUZZY_MODE));
            this.setReadWriteMode((AccessRestriction) this.getUpgradeable().getConfigManager().getSetting(Settings.ACCESS));
            this.setStorageFilter((StorageFilter) this.getUpgradeable().getConfigManager().getSetting(Settings.STORAGE_FILTER));
        }

        this.standardDetectAndSendChanges();
    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        final int upgrades = this.getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);

        return upgrades > idx;
    }

    public void clear() {
        IGasInventory h = this.getUpgradeable().getConfig();
        for (int i = 0; i < h.size(); ++i) {
            h.setGas(i, null);
        }
        this.detectAndSendChanges();
    }

    public void partition() {
        IGasInventory h = this.getUpgradeable().getConfig();

        final IMEInventory<IAEGasStack> cellInv = this.getUpgradeable().getInternalHandler();

        Iterator<IAEGasStack> i = new NullIterator<>();
        if (cellInv != null) {
            final IItemList<IAEGasStack> list = cellInv.getAvailableItems(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList());
            i = list.iterator();
        }

        for (int x = 0; x < h.size(); x++) {
            if (i.hasNext() && this.isSlotEnabled((x / 9) - 2)) {
                IAEGasStack gas = i.next();
                h.setGas(x, gas == null ? null : gas.getGasStack());
            } else {
                h.setGas(x, null);
            }
        }
        this.detectAndSendChanges();
    }

    public AccessRestriction getReadWriteMode() {
        return this.rwMode;
    }

    private void setReadWriteMode(final AccessRestriction rwMode) {
        this.rwMode = rwMode;
    }

    public StorageFilter getStorageFilter() {
        return this.storageFilter;
    }

    private void setStorageFilter(final StorageFilter storageFilter) {
        this.storageFilter = storageFilter;
    }

    @Override
    public IGasInventory getGasConfigInventory() {
        return this.getUpgradeable().getConfig();
    }

}
