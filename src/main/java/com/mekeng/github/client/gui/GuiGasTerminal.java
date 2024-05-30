package com.mekeng.github.client.gui;

import appeng.api.config.Settings;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.client.gui.AEBaseMEGui;
import appeng.client.gui.widgets.GuiImgButton;
import appeng.client.gui.widgets.GuiScrollbar;
import appeng.client.gui.widgets.ISortSource;
import appeng.client.gui.widgets.MEGuiTextField;
import appeng.core.localization.GuiText;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketInventoryAction;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.render.GasStackSizeRenderer;
import com.mekeng.github.client.slots.SlotGasME;
import com.mekeng.github.common.container.ContainerGasTerminal;
import com.mekeng.github.common.me.client.GasRepo;
import com.mekeng.github.common.me.client.RepoSlot;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.network.packet.CGenericPacket;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.Gas;
import mekanism.client.render.MekanismRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class GuiGasTerminal extends AEBaseMEGui implements ISortSource, IConfigManagerHost {
    private final List<SlotGasME> meGasSlots = new LinkedList<>();
    private final GasRepo repo;
    private final IConfigManager configSrc;
    private final ContainerGasTerminal container;
    private final GasStackSizeRenderer gasStackSizeRenderer = new GasStackSizeRenderer();
    private final int offsetX = 9;
    private final int rows = 6;
    private final int perRow = 9;

    protected ITerminalHost terminal;

    private MEGuiTextField searchField;
    private GuiImgButton sortByBox;
    private GuiImgButton sortDirBox;
    protected int jeiOffset = Loader.isModLoaded("jei") ? 24 : 0;

    public GuiGasTerminal(final InventoryPlayer inventoryPlayer, final ITerminalHost te) {
        this(inventoryPlayer, te, new ContainerGasTerminal(inventoryPlayer, te));
    }

    public GuiGasTerminal(InventoryPlayer inventoryPlayer, final ITerminalHost te, final ContainerGasTerminal c) {
        super(c);
        this.terminal = te;
        this.xSize = 190;
        this.ySize = 222;
        final GuiScrollbar scrollbar = new GuiScrollbar();
        this.setScrollBar(scrollbar);
        this.repo = new GasRepo(scrollbar, this);
        this.configSrc = ((IConfigurableObject) this.inventorySlots).getConfigManager();
        (this.container = (ContainerGasTerminal) this.inventorySlots).setGui(this);
    }

    protected String getName() {
        return "gui.mekeng.gas_terminal";
    }

    @Override
    public void initGui() {
        this.mc.player.openContainer = this.inventorySlots;
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.searchField = new MEGuiTextField(this.fontRenderer, this.guiLeft + 80, this.guiTop + 4, 90, 12);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(25);
        this.searchField.setTextColor(0xFFFFFF);
        this.searchField.setSelectionColor(0xFF99FF99);
        this.searchField.setVisible(true);

        int offset = this.guiTop;

        this.buttonList.add(this.sortByBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_BY, this.configSrc.getSetting(Settings.SORT_BY)));
        offset += 20;

        this.buttonList.add(this.sortDirBox = new GuiImgButton(this.guiLeft - 18, offset, Settings.SORT_DIRECTION, this.configSrc.getSetting(Settings.SORT_DIRECTION)));

        for (int y = 0; y < this.rows; y++) {
            for (int x = 0; x < this.perRow; x++) {
                SlotGasME slot = new SlotGasME(new RepoSlot(this.repo, x + y * this.perRow, this.offsetX + x * 18, 18 + y * 18));
                this.getMeGasSlots().add(slot);
                this.inventorySlots.inventorySlots.add(slot);
            }
        }
        this.setScrollBar();
    }

    @Override
    public void drawFG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(I18n.format(this.getName())), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);
    }

    @Override
    public void drawBG(int offsetX, int offsetY, int mouseX, int mouseY) {
        this.bindTexture(this.getBackground());
        final int x_width = 197;
        this.drawTexturedModalRect(offsetX, offsetY, 0, 0, x_width, 18);

        for (int x = 0; x < 6; x++) {
            this.drawTexturedModalRect(offsetX, offsetY + 18 + x * 18, 0, 18, x_width, 18);
        }

        this.drawTexturedModalRect(offsetX, offsetY + 16 + 6 * 18, 0, 106 - 18 - 18, x_width, 99 + 77);

        if (this.searchField != null) {
            this.searchField.drawTextBox();
        }
    }

    @Override
    public void drawSlot(Slot s) {
        if (s instanceof SlotGasME && ((SlotGasME) s).shouldRenderAsGas()) {
            final SlotGasME slot = (SlotGasME) s;
            final IAEGasStack gs = slot.getAEGasStack();
            if (gs != null && this.isPowered()) {
                GlStateManager.enableLighting();
                GlStateManager.enableBlend();
                final Gas gas = gs.getGas();
                mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                final TextureAtlasSprite sprite = gas.getSprite();
                // Set color for dynamic gases
                // Convert int color to RGB
                MekanismRenderer.color(gas);
                this.drawTexturedModalRect(s.xPos, s.yPos, sprite, 16, 16);
                MekanismRenderer.resetColor();
                this.gasStackSizeRenderer.renderStackSize(this.fontRenderer, gs, s.xPos, s.yPos);
            } else if (!this.isPowered()) {
                drawRect(s.xPos, s.yPos, 16 + s.xPos, 16 + s.yPos, 0x66111111);
            }
        } else {
            super.drawSlot(s);
        }
    }

    @Override
    public void updateScreen() {
        this.repo.setPower(this.container.isPowered());
        super.updateScreen();
    }

    @Override
    protected void renderHoveredToolTip(int mouseX, int mouseY) {
        final Slot slot = this.getSlot(mouseX, mouseY);

        if (slot instanceof SlotGasME && slot.isEnabled()) {
            final SlotGasME gasSlot = (SlotGasME) slot;

            if (gasSlot.getAEGasStack() != null && gasSlot.shouldRenderAsGas()) {
                final IAEGasStack gasStack = gasSlot.getAEGasStack();
                final String formattedAmount = NumberFormat.getNumberInstance(Locale.US).format(gasStack.getStackSize() / 1000.0) + " B";

                final String modName = "" + TextFormatting.BLUE + TextFormatting.ITALIC + Loader.instance()
                        .getIndexedModList()
                        .get(Utils.getGasModID(gasStack))
                        .getName();

                final List<String> list = new ArrayList<>();

                list.add(gasStack.getGas().getLocalizedName());
                list.add(formattedAmount);
                list.add(modName);

                this.drawHoveringText(list, mouseX, mouseY);

                return;
            }
        }
        super.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton btn) {
        if (btn instanceof GuiImgButton) {
            final boolean backwards = Mouse.isButtonDown(1);
            final GuiImgButton iBtn = (GuiImgButton) btn;

            if (iBtn.getSetting() != Settings.ACTIONS) {
                final Enum<?> cv = iBtn.getCurrentValue();
                final Enum<?> next = Platform.rotateEnum(cv, backwards, iBtn.getSetting().getPossibleValues());
                try {
                    NetworkHandler.instance().sendToServer(new PacketValueConfig(iBtn.getSetting().name(), next.name()));
                } catch (final IOException e) {
                    MekEng.log.debug(e);
                }
                iBtn.set(next);
            }
        }
    }

    @Override
    protected void handleMouseClick(Slot slot, int slotIdx, int mouseButton, ClickType clickType) {
        if (slot instanceof SlotGasME) {
            final SlotGasME meSlot = (SlotGasME) slot;

            if (clickType == ClickType.PICKUP) {
                if (mouseButton == 0 && meSlot.getHasStack()) {
                    this.container.setTargetGasStack(meSlot.getAEGasStack());
                    MekEng.proxy.netHandler.sendToServer(new CGenericPacket("set_target", meSlot.getAEGasStack()));
                    MekEng.log.debug("mouse0 GUI STACK SIZE %s", meSlot.getAEGasStack().getStackSize());
                    NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.FILL_ITEM, slot.slotNumber, 0));
                } else {
                    this.container.setTargetGasStack(meSlot.getAEGasStack());
                    MekEng.proxy.netHandler.sendToServer(new CGenericPacket("set_target", meSlot.getAEGasStack()));
                    if (meSlot.getAEGasStack() != null) {
                        MekEng.log.debug("mouse1 GUI STACK SIZE %s", meSlot.getAEGasStack().getStackSize());
                    }
                    NetworkHandler.instance().sendToServer(new PacketInventoryAction(InventoryAction.EMPTY_ITEM, slot.slotNumber, 0));
                }
            }
            return;
        }
        super.handleMouseClick(slot, slotIdx, mouseButton, clickType);
    }

    @Override
    protected void keyTyped(final char character, final int key) throws IOException {
        if (!this.checkHotbarKeys(key)) {
            if (character == ' ' && this.searchField.getText().isEmpty()) {
                return;
            }
            if (this.searchField.textboxKeyTyped(character, key)) {
                this.repo.setSearchString(this.searchField.getText());
                this.repo.updateView();
                this.setScrollBar();
            } else {
                super.keyTyped(character, key);
            }
        }
    }

    @Override
    protected void mouseClicked(final int xCoord, final int yCoord, final int btn) throws IOException {
        this.searchField.mouseClicked(xCoord, yCoord, btn);
        if (btn == 1 && this.searchField.isMouseIn(xCoord, yCoord)) {
            this.searchField.setText("");
            this.repo.setSearchString("");
            this.repo.updateView();
            this.setScrollBar();
        }
        super.mouseClicked(xCoord, yCoord, btn);
    }

    public void postUpdate(final List<IAEGasStack> list) {
        for (final IAEGasStack is : list) {
            this.repo.postUpdate(is);
        }
        this.repo.updateView();
        this.setScrollBar();
    }

    private void setScrollBar() {
        this.getScrollBar().setTop(18).setLeft(175).setHeight(this.rows * 18 - 2);
        this.getScrollBar().setRange(0, (this.repo.size() + this.perRow - 1) / this.perRow - this.rows, Math.max(1, this.rows / 6));
    }

    @Override
    public Enum<?> getSortBy() {
        return this.configSrc.getSetting(Settings.SORT_BY);
    }

    @Override
    public Enum<?> getSortDir() {
        return this.configSrc.getSetting(Settings.SORT_DIRECTION);
    }

    @Override
    public Enum<?> getSortDisplay() {
        return this.configSrc.getSetting(Settings.VIEW_MODE);
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.sortByBox != null) {
            this.sortByBox.set(this.configSrc.getSetting(Settings.SORT_BY));
        }

        if (this.sortDirBox != null) {
            this.sortDirBox.set(this.configSrc.getSetting(Settings.SORT_DIRECTION));
        }

        this.repo.updateView();
    }

    protected List<SlotGasME> getMeGasSlots() {
        return this.meGasSlots;
    }

    @Override
    protected boolean isPowered() {
        return this.repo.hasPower();
    }

    protected String getBackground() {
        return "guis/terminal.png";
    }

    @Override
    public List<Rectangle> getJEIExclusionArea() {
        List<Rectangle> exclusionArea = new ArrayList<>();

        int yOffset = guiTop + 8 + jeiOffset;

        int visibleButtons = 2;
        Rectangle sortDir = new Rectangle(guiLeft - 18, yOffset, 20, visibleButtons * 20 + visibleButtons - 2);
        exclusionArea.add(sortDir);

        return exclusionArea;
    }

}
