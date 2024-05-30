package com.mekeng.github.client.gui;

import appeng.api.util.IConfigManager;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketSwitchGuis;
import appeng.util.IConfigManagerHost;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.client.slots.SlotGasTank;
import com.mekeng.github.common.container.ContainerGasInterface;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.network.packet.CGenericPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiGasInterface extends GuiGasUpgradeable implements IConfigManagerHost {

    private final IGasInterfaceHost host;
    private final ContainerGasInterface container;
    private GuiTabButton priority;

    public GuiGasInterface(final InventoryPlayer ip, final IGasInterfaceHost te) {
        super(new ContainerGasInterface(ip, te));
        this.ySize = 231;
        this.xSize = 245;
        this.host = te;
        (this.container = (ContainerGasInterface) this.inventorySlots).setGui(this);
    }

    @Override
    public void initGui() {
        super.initGui();

        final IGasInventory configGases = this.host.getDualityGasInterface().getConfig();
        final IGasInventory gasTank = this.host.getDualityGasInterface().getTanks();

        for (int i = 0; i < DualityGasInterface.NUMBER_OF_TANKS; ++i) {
            this.guiSlots.add(new SlotGasTank(gasTank, i, DualityGasInterface.NUMBER_OF_TANKS + i, 8 + 18 * i, 53, 16, 68));
            this.guiSlots.add(new SlotGas(configGases, i, i, 8 + 18 * i, 35));
        }

        this.priority = new GuiTabButton(this.getGuiLeft() + 154, this.getGuiTop(), 2 + 4 * 16, GuiText.Priority.getLocal(), this.itemRender);
        this.buttonList.add(this.priority);
    }

    @Override
    protected void addButtons() {
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(this.getGuiName()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.Config.getLocal(), 8, 6 + 11 + 7, 4210752);
        this.fontRenderer.drawString(I18n.format("tooltip.mekeng.stored_gas"), 8, 6 + 112 + 7, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    protected String getGuiName() {
        return I18n.format("gui.mekeng.gas_interface");
    }

    @Override
    protected String getBackground() {
        return "guis/interfacefluidextendedlife.png";
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);
        if (btn == this.priority) {
            NetworkHandler.instance().sendToServer(new PacketSwitchGuis(GuiBridge.GUI_PRIORITY));
        }
    }

    @Override
    protected void mouseClicked(int xCoord, int yCoord, int btn) throws IOException {
        for (GuiCustomSlot slot : this.guiSlots) {
            if (slot instanceof SlotGasTank) {
                if (this.isPointInRegion(slot.xPos(), slot.yPos(), slot.getWidth(), slot.getHeight(), xCoord, yCoord) && slot.canClick(this.mc.player)) {
                    IAEGasStack gas = AEGasStack.of(((SlotGasTank) slot).getGasStack());
                    this.container.setTargetGasStack(gas);
                    slot.slotClicked(this.mc.player.inventory.getItemStack(), btn);
                    MekEng.proxy.netHandler.sendToServer(new CGenericPacket("set_target", gas));
                    return;
                }
            }
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {

    }
}
