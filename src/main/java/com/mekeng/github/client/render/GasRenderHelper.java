package com.mekeng.github.client.render;

import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import com.mekeng.github.common.me.data.IAEGasStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GasRenderHelper {

    private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

    public static void renderGas2d(GasStack gasStack) {
        if (gasStack != null) {
            GlStateManager.pushMatrix();
            Gas gas = gasStack.getGas();
            MekanismRenderer.color(gas);
            TextureAtlasSprite sprite = gas.getSprite();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            float width = 0.4f;
            float height = 0.4f;
            float z = 0.0001f;
            float x = -0.2f;
            float y = -0.25f;
            buf.begin(7, DefaultVertexFormats.POSITION_TEX);
            float uMin = sprite.getInterpolatedU(16.0f - width * 16.0f);
            float uMax = sprite.getInterpolatedU(width * 16.0f);
            float vMin = sprite.getMinV();
            float vMax = sprite.getInterpolatedV(height * 16.0f);
            buf.pos(x, y, z).tex(uMin, vMin).endVertex();
            buf.pos(x, (y + height), z).tex(uMin, vMax).endVertex();
            buf.pos((x + width), (y + height), z).tex(uMax, vMax).endVertex();
            buf.pos((x + width), y, z).tex(uMax, vMin).endVertex();
            tess.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            MekanismRenderer.resetColor();
            GlStateManager.popMatrix();
        }
    }

    public static void renderGas2dWithAmount(IAEGasStack gasStack, float scale, float spacing) {
        GasStack renderStack = gasStack.getGasStack();
        renderGas2d(renderStack);
        long stackSize = gasStack.getStackSize() / 1000L;
        String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm(stackSize) + "B";
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int width = fr.getStringWidth(renderedStackSize);
        GlStateManager.translate(0.0f, spacing, 0.0f);
        GlStateManager.scale(1.0f / 62.0f, 1.0f / 62.0f, 1.0f / 62.0f);
        GlStateManager.translate(-0.5f * (float)width, 0.0f, 0.5f);
        fr.drawString(renderedStackSize, 0, 0, 0);
    }


}
