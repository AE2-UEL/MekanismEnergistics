package com.mekeng.github.client.slots;

import appeng.container.slot.IOptionalSlotHost;
import com.mekeng.github.common.me.inventory.IGasInventory;
import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.GlStateManager;

public class SlotOptionalGas extends SlotGas {
    private final IOptionalSlotHost containerBus;
    private final int groupNum;
    private final int srcX;
    private final int srcY;

    public SlotOptionalGas(IGasInventory gases, final IOptionalSlotHost containerBus, int slot, int id, int groupNum, int x, int y, int xoffs, int yoffs) {
        super(gases, slot, id, x + xoffs * 18, y + yoffs * 18);
        this.containerBus = containerBus;
        this.groupNum = groupNum;
        this.srcX = x;
        this.srcY = y;
    }

    @Override
    public boolean isSlotEnabled() {
        if (this.containerBus == null) {
            return false;
        }
        return this.containerBus.isSlotEnabled(this.groupNum);
    }

    @Override
    public GasStack getGasStack() {
        if (!this.isSlotEnabled() && super.getGasStack() != null) {
            this.setGasStack(null);
        }
        return super.getGasStack();
    }

    @Override
    public void drawBackground(int guileft, int guitop) {
        GlStateManager.enableBlend();
        if (this.isSlotEnabled()) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.4F);
        }
        this.drawTexturedModalRect(guileft + this.xPos() - 1, guitop + this.yPos() - 1, this.srcX - 1, this.srcY - 1, this.getWidth() + 2, this.getHeight() + 2);
    }
}
