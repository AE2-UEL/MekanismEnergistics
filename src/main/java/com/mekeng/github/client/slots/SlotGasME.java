package com.mekeng.github.client.slots;

import com.mekeng.github.common.me.client.RepoSlot;
import com.mekeng.github.common.me.data.IAEGasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

import javax.annotation.Nonnull;

public class SlotGasME extends SlotItemHandler {
    private final RepoSlot slot;

    public SlotGasME(RepoSlot slot) {
        super(null, 0, slot.getX(), slot.getY());
        this.slot = slot;
    }

    public boolean shouldRenderAsGas() {
        return true;
    }

    public IAEGasStack getAEGasStack() {
        return this.slot.hasPower() ? this.slot.getAEStack() : null;
    }

    @Override
    public boolean isItemValid(@Nonnull ItemStack stack) {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean getHasStack() {
        if (this.slot.hasPower()) {
            return this.getAEGasStack() != null;
        } else {
            return false;
        }
    }

    @Override
    public void putStack(@Nonnull ItemStack stack) {
        // NO-OP
    }

    @Override
    public int getSlotStackLimit() {
        return 0;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int amt) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isHere(@Nonnull IInventory inv, int slotIn) {
        return false;
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        return false;
    }

}