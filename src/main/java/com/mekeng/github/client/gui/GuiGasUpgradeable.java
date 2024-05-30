package com.mekeng.github.client.gui;

import appeng.api.implementations.IUpgradeableHost;
import appeng.client.gui.implementations.GuiUpgradeable;
import appeng.client.gui.widgets.GuiCustomSlot;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.core.localization.GuiText;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.slots.SlotGas;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.network.packet.CGasSlotSync;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.GasStack;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class GuiGasUpgradeable extends GuiUpgradeable {

    public GuiGasUpgradeable(InventoryPlayer inventoryPlayer, IUpgradeableHost te) {
        super(inventoryPlayer, te);
    }

    public GuiGasUpgradeable(ContainerUpgradeable te) {
        super(te);
    }

    @Override
    public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(this.getGuiDisplayName(this.getGuiName()), 8, 6, 4210752);
        this.fontRenderer.drawString(GuiText.inventory.getLocal(), 8, this.ySize - 96 + 3, 4210752);

        if (this.redstoneMode != null) {
            this.redstoneMode.set(this.cvb.getRedStoneMode());
        }

        if (this.fuzzyMode != null) {
            this.fuzzyMode.set(this.cvb.getFuzzyMode());
        }

        if (this.craftMode != null) {
            this.craftMode.set(this.cvb.getCraftingMode());
        }

        if (this.schedulingMode != null) {
            this.schedulingMode.set(this.cvb.getSchedulingMode());
        }
    }

    @Override
    protected String getBackground() {
        return "guis/gas_bus.png";
    }

    protected abstract String getGuiName();

    @Override
    public List<IGhostIngredientHandler.Target<?>> getPhantomTargets(Object ingredient) {
        GasStack gas = null;
        if (ingredient instanceof GasStack) {
            gas = (GasStack) ingredient;
        } else if (ingredient instanceof ItemStack) {
            gas = Utils.getGasFromItem((ItemStack) ingredient);
        }
        if (gas != null) {
            final GasStack imGas = gas;
            this.mapTargetSlot.clear();
            List<IGhostIngredientHandler.Target<?>> targets = new ArrayList<>();
            List<SlotGas> slots = new ArrayList<>();
            if (!this.getGuiSlots().isEmpty()) {
                for (GuiCustomSlot slot : this.getGuiSlots()) {
                    if (slot instanceof SlotGas) {
                        slots.add((SlotGas) slot);
                    }
                }
            }
            for (SlotGas slot : slots) {
                IGhostIngredientHandler.Target<Object> targetItem = new IGhostIngredientHandler.Target<Object>() {
                    @Nonnull
                    @Override
                    public Rectangle getArea() {
                        if (slot.isSlotEnabled()) {
                            return new Rectangle(getGuiLeft() + slot.xPos(), getGuiTop() + slot.yPos(), 16, 16);
                        }
                        return new Rectangle();
                    }

                    @Override
                    public void accept(@Nonnull Object o) {
                        MekEng.proxy.netHandler.sendToServer(new CGasSlotSync(Collections.singletonMap(slot.getId(), AEGasStack.of(imGas))));
                    }
                };
                targets.add(targetItem);
                this.mapTargetSlot.putIfAbsent(targetItem, slot);
            }
            return targets;
        } else {
            return super.getPhantomTargets(ingredient);
        }
    }

}
