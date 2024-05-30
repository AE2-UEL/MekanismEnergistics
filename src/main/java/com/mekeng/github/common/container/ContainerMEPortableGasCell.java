package com.mekeng.github.common.container;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.container.interfaces.IInventorySlotAware;
import com.mekeng.github.common.me.storage.IPortableGasCell;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class ContainerMEPortableGasCell extends ContainerGasTerminal implements IInventorySlotAware {

    protected final IPortableGasCell portableCell;
    private final int slot;
    private int ticks = 0;

    public ContainerMEPortableGasCell(InventoryPlayer ip, IPortableGasCell guiObject) {
        super(ip, guiObject, guiObject, false);
        if (guiObject != null) {
            final int slotIndex = ((IInventorySlotAware) guiObject).getInventorySlot();
            if (!((IInventorySlotAware) guiObject).isBaubleSlot()) {
                this.lockPlayerInventorySlot(slotIndex);
            }
            this.slot = slotIndex;
        } else {
            this.slot = -1;
            this.lockPlayerInventorySlot(ip.currentItem);
        }

        this.bindPlayerInventory(ip, 0, 222 - 82);

        this.portableCell = guiObject;
    }

    @Override
    public void detectAndSendChanges() {
        final ItemStack currentItem = this.slot < 0 ? this.getPlayerInv().getCurrentItem() : this.getPlayerInv().getStackInSlot(this.slot);

        if (this.portableCell == null || currentItem.isEmpty()) {
            this.setValidContainer(false);
        } else if (!this.portableCell.getItemStack().isEmpty() && currentItem != this.portableCell.getItemStack()) {
            if (!ItemStack.areItemsEqual(this.portableCell.getItemStack(), currentItem)) {
                this.setValidContainer(false);
            }
        }

        // drain 1 ae t
        this.ticks++;
        if (this.ticks > 10 && this.portableCell != null) {
            this.portableCell.extractAEPower(this.getPowerMultiplier() * this.ticks, Actionable.MODULATE, PowerMultiplier.CONFIG);
            this.ticks = 0;
        }
        super.detectAndSendChanges();
    }

    private double getPowerMultiplier() {
        return 0.5;
    }

    @Override
    public int getInventorySlot() {
        return this.slot;
    }

}