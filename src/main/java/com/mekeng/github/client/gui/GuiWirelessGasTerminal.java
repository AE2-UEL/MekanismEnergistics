package com.mekeng.github.client.gui;

import appeng.helpers.WirelessTerminalGuiObject;
import com.mekeng.github.common.container.ContainerWirelessGasTerminal;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.InventoryPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiWirelessGasTerminal extends GuiGasTerminal {

    public GuiWirelessGasTerminal(final InventoryPlayer inventoryPlayer, final WirelessTerminalGuiObject te) {
        super(inventoryPlayer, te, new ContainerWirelessGasTerminal(inventoryPlayer, te));
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture("guis/wirelessupgrades.png");
        Gui.drawModalRectWithCustomSizedTexture(offsetX + 175, offsetY + 131, 0, 0, 32, 32, 32, 32);
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    public java.util.List<Rectangle> getJEIExclusionArea() {
        List<Rectangle> ea = new ArrayList<>();
        ea.add(new Rectangle(this.guiLeft + 174,
                this.guiTop + 131,
                32,
                32));
        return ea;
    }

    @Override
    protected String getName() {
        return "gui.mekeng.wireless_gas_terminal";
    }

}
