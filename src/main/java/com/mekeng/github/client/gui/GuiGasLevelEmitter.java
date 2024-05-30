package com.mekeng.github.client.gui;

import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiNumberBox;
import appeng.core.AEConfig;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.common.container.ContainerGasLevelEmitter;
import com.mekeng.github.common.part.PartGasLevelEmitter;
import com.mekeng.github.network.packet.CGenericPacket;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;

import java.io.IOException;

public class GuiGasLevelEmitter extends GuiGasUpgradeable {
    private final PartGasLevelEmitter levelEmitter;
    private GuiNumberBox level;

    private GuiButton plus1;
    private GuiButton plus10;
    private GuiButton plus100;
    private GuiButton plus1000;
    private GuiButton minus1;
    private GuiButton minus10;
    private GuiButton minus100;
    private GuiButton minus1000;

    public GuiGasLevelEmitter(final InventoryPlayer inventoryPlayer, final PartGasLevelEmitter te) {
        super(new ContainerGasLevelEmitter(inventoryPlayer, te));
        this.levelEmitter = te;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.level = new GuiNumberBox(this.fontRenderer, this.guiLeft + 24, this.guiTop + 43, 79, this.fontRenderer.FONT_HEIGHT, Long.class);
        this.level.setEnableBackgroundDrawing(false);
        this.level.setMaxStringLength(16);
        this.level.setTextColor(0xFFFFFF);
        this.level.setVisible(true);
        this.level.setFocused(true);
        ((ContainerGasLevelEmitter) this.inventorySlots).setTextField(this.level);

        final int y = 40;
        final int x = 80 + 44;
        this.guiSlots.add(new SlotGas(this.levelEmitter.getConfig(), 0, 0, x, y));
    }

    @Override
    protected void addButtons() {
        this.redstoneMode = new GuiImgButton(this.guiLeft - 18, this.guiTop + 28, Settings.REDSTONE_EMITTER, RedstoneMode.LOW_SIGNAL);

        final int a = AEConfig.instance().levelByMillyBuckets(0);
        final int b = AEConfig.instance().levelByMillyBuckets(1);
        final int c = AEConfig.instance().levelByMillyBuckets(2);
        final int d = AEConfig.instance().levelByMillyBuckets(3);

        this.buttonList.add(this.plus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 17, 22, 20, "+" + a));
        this.buttonList.add(this.plus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 17, 28, 20, "+" + b));
        this.buttonList.add(this.plus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 17, 32, 20, "+" + c));
        this.buttonList.add(this.plus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 17, 38, 20, "+" + d));

        this.buttonList.add(this.minus1 = new GuiButton(0, this.guiLeft + 20, this.guiTop + 59, 22, 20, "-" + a));
        this.buttonList.add(this.minus10 = new GuiButton(0, this.guiLeft + 48, this.guiTop + 59, 28, 20, "-" + b));
        this.buttonList.add(this.minus100 = new GuiButton(0, this.guiLeft + 82, this.guiTop + 59, 32, 20, "-" + c));
        this.buttonList.add(this.minus1000 = new GuiButton(0, this.guiLeft + 120, this.guiTop + 59, 38, 20, "-" + d));

        this.buttonList.add(this.redstoneMode);
    }

    @Override
    public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        super.drawBG(offsetX, offsetY, mouseX, mouseY);
        this.level.drawTextBox();
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        if (isPointInRegion(24, 43, 89, this.fontRenderer.FONT_HEIGHT, mouseX, mouseY))
            drawTooltip(mouseX - guiLeft - 7, mouseY - guiTop + 25, "Amount in millibuckets");
        super.drawFG(offsetX, offsetY, mouseX, mouseY);
    }

    @Override
    protected boolean drawUpgrades() {
        return false;
    }

    @Override
    protected String getBackground() {
        return "guis/lvlemitter.png";
    }

    @Override
    protected String getGuiName() {
        return I18n.format("gui.mekeng.gas_level_emitter");
    }

    @Override
    protected void handleButtonVisibility() {
        // NO-OP
    }

    @Override
    protected void actionPerformed(final GuiButton btn) throws IOException {
        super.actionPerformed(btn);
        final boolean isPlus = btn == this.plus1 || btn == this.plus10 || btn == this.plus100 || btn == this.plus1000;
        final boolean isMinus = btn == this.minus1 || btn == this.minus10 || btn == this.minus100 || btn == this.minus1000;
        if (isPlus || isMinus) {
            this.addQty(this.getQty(btn));
        }
    }

    private void addQty(final long i) {
        try {
            String Out = this.level.getText();

            boolean Fixed = false;
            while (Out.startsWith("0") && Out.length() > 1) {
                Out = Out.substring(1);
                Fixed = true;
            }

            if (Fixed) {
                this.level.setText(Out);
            }

            if (Out.isEmpty()) {
                Out = "0";
            }

            long result = Long.parseLong(Out);
            result += i;
            if (result < 0) {
                result = 0;
            }

            this.level.setText(Out = Long.toString(result));
            MekEng.proxy.netHandler.sendToServer(new CGenericPacket("set_level", Long.parseLong(Out)));
        } catch (final NumberFormatException e) {
            // nope..
            this.level.setText("0");
        }
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if ((key == 211 || key == 205 || key == 203 || key == 14 || Character.isDigit(character)) && this.level.textboxKeyTyped(character, key)) {
                String Out = this.level.getText();

                boolean Fixed = false;
                while (Out.startsWith("0") && Out.length() > 1) {
                    Out = Out.substring(1);
                    Fixed = true;
                }

                if (Fixed) {
                    this.level.setText(Out);
                }

                if (Out.isEmpty()) {
                    Out = "0";
                }
                MekEng.proxy.netHandler.sendToServer(new CGenericPacket("set_level", Long.parseLong(Out)));
            } else {
                super.keyTyped(character, key);
            }
        }
    }
}
