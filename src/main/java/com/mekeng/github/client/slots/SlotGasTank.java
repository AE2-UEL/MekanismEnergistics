package com.mekeng.github.client.slots;

import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.helpers.InventoryAction;
import com.mekeng.github.common.me.inventory.IGasInventory;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SlotGasTank extends GuiCustomSlot implements ITooltip {
    private final IGasInventory tank;
    private final int slot;
    private final int width;
    private final int height;
    private boolean darkened = false;

    public SlotGasTank(IGasInventory tank, int slot, int id, int x, int y, int w, int h) {
        super(id, x, y);
        this.tank = tank;
        this.slot = slot;
        this.width = w;
        this.height = h;
    }

    public SlotGasTank(IGasInventory tank, int slot, int id, int x, int y, int w, int h, boolean darkened) {
        super(id, x, y);
        this.tank = tank;
        this.slot = slot;
        this.width = w;
        this.height = h;
        this.darkened = darkened;
    }

    @Override
    public void drawContent(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        final GasStack fs = this.getGasStack();
        if (fs != null) {
            GlStateManager.enableLighting();
            GlStateManager.enableBlend();

            //drawRect( this.x, this.y, this.x + this.width, this.y + this.height, AEColor.GRAY.blackVariant | 0xFF000000 );

            final GasStack gas = this.tank.getGasStack(this.slot);
            if (gas != null && gas.amount > 0) {
                mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

                float red = (gas.getGas().getTint() >> 16 & 255) / 255.0F;
                float green = (gas.getGas().getTint() >> 8 & 255) / 255.0F;
                float blue = (gas.getGas().getTint() & 255) / 255.0F;
                if (darkened) {
                    red = red * 0.4F;
                    green = green * 0.4F;
                    blue = blue * 0.4F;
                }
                GlStateManager.color(red, green, blue);
                TextureAtlasSprite sprite = gas.getGas().getSprite();
                int scaledHeight = (int) (this.height * ((float) gas.amount / this.tank.getTanks()[this.slot].getMaxGas()));
                scaledHeight = Math.min(this.height, scaledHeight);

                int iconHeightRemainder = scaledHeight % 16;
                if (iconHeightRemainder > 0) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder, sprite, 16, iconHeightRemainder);
                }
                for (int i = 0; i < scaledHeight / 16; i++) {
                    this.drawTexturedModalRect(this.xPos(), this.yPos() + this.getHeight() - iconHeightRemainder - (i + 1) * 16, sprite, 16, 16);
                }
                MekanismRenderer.resetColor();
            }
        }
    }

    @Override
    public String getMessage() {
        final GasStack gas = this.tank.getGasStack(this.slot);
        if (gas != null && gas.amount > 0) {
            String desc = gas.getGas().getLocalizedName();
            return desc + "\n" + gas.amount + "/" + this.tank.getTanks()[this.slot].getMaxGas() + "mB";
        }
        return null;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public GasStack getGasStack() {
        return this.tank.getGasStack(this.slot);
    }

    @Override
    public void slotClicked(ItemStack clickStack, final int mouseButton) {
        if (getGasStack() != null) {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot, id));
        } else {
            NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot, id));
        }
    }

}
