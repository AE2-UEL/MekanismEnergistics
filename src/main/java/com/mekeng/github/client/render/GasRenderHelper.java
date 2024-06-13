package com.mekeng.github.client.render;

import appeng.util.IWideReadableNumberConverter;
import appeng.util.ReadableNumberConverter;
import com.mekeng.github.common.me.data.IAEGasStack;
import mekanism.api.gas.GasStack;
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
    public static void renderGas2d(GasStack gasStack, float scale) {
        if (gasStack != null) {
            GlStateManager.pushMatrix();
            int color = gasStack.getGas().getTint();
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color & 255) / 255.0F;
            TextureAtlasSprite sprite = gasStack.getGas().getSprite();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buf = tess.getBuffer();
            float width = 0.4F;
            float height = 0.4F;
            float alpha = 1.0F;
            float z = 1.0E-4F;
            float x = -0.2F;
            float y = -0.25F;
            buf.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            double uMin = (double)sprite.getInterpolatedU(16.0 - (double)width * 16.0);
            double uMax = (double)sprite.getInterpolatedU((double)width * 16.0);
            double vMin = (double)sprite.getMinV();
            double vMax = (double)sprite.getInterpolatedV((double)height * 16.0);
            buf.pos((double)x, (double)y, (double)z).tex(uMin, vMin).color(r, g, b, alpha).endVertex();
            buf.pos((double)x, (double)(y + height), (double)z).tex(uMin, vMax).color(r, g, b, alpha).endVertex();
            buf.pos((double)(x + width), (double)(y + height), (double)z).tex(uMax, vMax).color(r, g, b, alpha).endVertex();
            buf.pos((double)(x + width), (double)y, (double)z).tex(uMax, vMin).color(r, g, b, alpha).endVertex();
            tess.draw();
            GlStateManager.enableLighting();
            GlStateManager.enableAlpha();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.popMatrix();
        }

    }

    public static void renderGas2dWithAmount(IAEGasStack gasStack, float scale, float spacing) {
        GasStack renderStack = gasStack.getGasStack();
        renderGas2d(renderStack, scale);
        long stackSize = gasStack.getStackSize() / 1000L;
        String renderedStackSize = NUMBER_CONVERTER.toWideReadableForm(stackSize) + "B";
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int width = fr.getStringWidth(renderedStackSize);
        GlStateManager.translate(0.0F, spacing, 0.0F);
        GlStateManager.scale(0.016129032F, 0.016129032F, 0.016129032F);
        GlStateManager.translate(-0.5F * (float)width, 0.0F, 0.5F);
        fr.drawString(renderedStackSize, 0, 0, 0);
    }


}
