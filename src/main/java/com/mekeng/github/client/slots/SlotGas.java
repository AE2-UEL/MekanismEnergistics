package com.mekeng.github.client.slots;

import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.slot.IJEITargetSlot;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.network.packet.CGasSlotSync;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Collections;

public class SlotGas extends GuiCustomSlot implements IJEITargetSlot {
    private final IGasInventory gases;
    private final int slot;

    public SlotGas(final IGasInventory gases, final int slot, final int id, final int x, final int y) {
        super(id, x, y);
        this.gases = gases;
        this.slot = slot;
    }

    @Override
    public void drawContent(final Minecraft mc, final int mouseX, final int mouseY, final float partialTicks) {
        final GasStack gs = this.getGasStack();
        if (gs != null) {
            GlStateManager.enableLighting();
            GlStateManager.enableBlend();
            final Gas gas = gs.getGas();
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            final TextureAtlasSprite sprite = gas.getSprite();
            // Set color for dynamic gases
            // Convert int color to RGB
            MekanismRenderer.color(gas);
            this.drawTexturedModalRect(this.xPos(), this.yPos(), sprite, this.getWidth(), this.getHeight());
            MekanismRenderer.resetColor();
        }
    }

    @Override
    public boolean canClick(final EntityPlayer player) {
        final ItemStack mouseStack = player.inventory.getItemStack();
        return mouseStack.isEmpty() || Utils.getGasHandler(mouseStack) != null;
    }

    @Override
    public void slotClicked(final ItemStack clickStack, int mouseButton) {
        if (clickStack.isEmpty() || mouseButton == 1) {
            this.setGasStack(null);
        } else if (mouseButton == 0) {
            final GasStack gas = Utils.getGasFromItem(clickStack);
            if (gas != null) {
                this.setGasStack(gas);
            }
        }
    }

    @Override
    public String getMessage() {
        final GasStack gas = this.getGasStack();
        if (gas != null) {
            return gas.getGas().getLocalizedName();
        }
        return null;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public GasStack getGasStack() {
        return this.gases.getGasStack(this.slot);
    }

    public void setGasStack(final GasStack stack) {
        this.gases.setGas(this.slot, stack);
        MekEng.proxy.netHandler.sendToServer(new CGasSlotSync(Collections.singletonMap(this.getId(), AEGasStack.of(stack))));
    }

    @Override
    public boolean needAccept() {
        return this.getGasStack() == null;
    }

}
