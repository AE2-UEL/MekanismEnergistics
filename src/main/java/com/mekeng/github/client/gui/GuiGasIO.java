package com.mekeng.github.client.gui;

import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.client.slots.SlotOptionalGas;
import com.mekeng.github.common.container.ContainerGasIO;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.part.PartSharedGasBus;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiGasIO extends GuiGasUpgradeable {
    private final PartSharedGasBus bus;

    public GuiGasIO(InventoryPlayer inventoryPlayer, PartSharedGasBus te) {
        super(new ContainerGasIO(inventoryPlayer, te));
        this.bus = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        final ContainerGasIO container = (ContainerGasIO) this.inventorySlots;
        final IGasInventory inv = this.bus.getConfig();
        final int y = 40;
        final int x = 80;

        this.guiSlots.add(new SlotGas(inv, 0, 0, x, y));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 1, 1, 1, x, y, -1, 0));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 2, 2, 1, x, y, 1, 0));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 3, 3, 1, x, y, 0, -1));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 4, 4, 1, x, y, 0, 1));

        this.guiSlots.add(new SlotOptionalGas(inv, container, 5, 5, 2, x, y, -1, -1));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 6, 6, 2, x, y, 1, -1));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 7, 7, 2, x, y, -1, 1));
        this.guiSlots.add(new SlotOptionalGas(inv, container, 8, 8, 2, x, y, 1, 1));
    }

    @Override
    protected String getGuiName() {
        return this.bus.isExport() ? I18n.format("gui.mekeng.gas_export") : I18n.format("gui.mekeng.gas_import");
    }

}
