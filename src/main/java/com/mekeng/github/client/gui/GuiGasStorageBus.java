package com.mekeng.github.client.gui;

import appeng.api.config.AccessRestriction;
import appeng.api.config.ActionItems;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketConfigButton;
import appeng.core.sync.packets.PacketSwitchGuis;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.client.slots.SlotOptionalGas;
import com.mekeng.github.common.container.ContainerGasStorageBus;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.part.PartGasStorageBus;
import com.mekeng.github.network.packet.CGenericPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiGasStorageBus extends GuiGasUpgradeable {
    private GuiImgButton rwMode;
    private GuiImgButton storageFilter;
    private GuiTabButton priority;
    private GuiImgButton partition;
    private GuiImgButton clear;
    private final PartGasStorageBus bus;

    public GuiGasStorageBus(InventoryPlayer inventoryPlayer, PartGasStorageBus te) {
        super(new ContainerGasStorageBus(inventoryPlayer, te));
        this.ySize = 251;
        this.bus = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        final int xo = 8;
        final int yo = 23 + 6;

        final IGasInventory config = this.bus.getConfig();
        final ContainerGasStorageBus container = (ContainerGasStorageBus) this.inventorySlots;

        for (int y = 0; y < 7; y++) {
            for (int x = 0; x < 9; x++) {
                final int idx = y * 9 + x;
                if (y < 2) {
                    this.guiSlots.add(new SlotGas(config, idx, idx, xo + x * 18, yo + y * 18));
                } else {
                    this.guiSlots.add(new SlotOptionalGas(config, container, idx, idx, y - 2, xo, yo, x, y));
                }
            }
        }
    }

    @Override
    protected void addButtons() {
        this.clear = new GuiImgButton(this.guiLeft - 18, this.guiTop + 8, Settings.ACTIONS, ActionItems.CLOSE);
        this.partition = new GuiImgButton(this.guiLeft - 18, this.guiTop + 28, Settings.ACTIONS, ActionItems.WRENCH);
        this.rwMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 48, Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.storageFilter = new GuiImgButton(this.guiLeft - 18, this.guiTop + 68, Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.fuzzyMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 88, Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);

        this.buttonList.add(this.priority = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender));

        this.buttonList.add(this.storageFilter);
        this.buttonList.add(this.fuzzyMode);
        this.buttonList.add(this.rwMode);
        this.buttonList.add(this.partition);
        this.buttonList.add(this.clear);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(this.getGuiName()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.storageFilter != null) {
            this.storageFilter.set(((ContainerGasStorageBus) this.cvb).getStorageFilter());
        }

        if (this.rwMode != null) {
            this.rwMode.set(((ContainerGasStorageBus) this.cvb).getReadWriteMode());
        }
    }

    @Override
    protected String getGuiName() {
        return I18n.format("gui.mekeng.gas_storage_bus");
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);

        final boolean backwards = Mouse.isButtonDown(1);

        if (btn == this.partition) {
            MekEng.proxy.netHandler.sendToServer(new CGenericPacket("partition"));
        } else if (btn == this.clear) {
            MekEng.proxy.netHandler.sendToServer(new CGenericPacket("clear"));
        } else if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        } else if (btn == this.rwMode) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(this.rwMode.getSetting(), backwards));
        } else if (btn == this.storageFilter) {
            NetworkHandler.instance().sendToServer(new PacketConfigButton(this.storageFilter.getSetting(), backwards));
        }
    }

    @Override
    protected String getBackground() {
        return "guis/storagebus.png";
    }

}
