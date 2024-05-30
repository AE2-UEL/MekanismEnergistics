package com.mekeng.github.common.part;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.parts.PartModel;
import appeng.parts.reporting.AbstractPartDisplay;
import appeng.util.Platform;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;

public class PartGasInterfaceConfigurationTerminal extends AbstractPartDisplay {

    public static final ResourceLocation MODEL_OFF = new ResourceLocation(MekEng.MODID, "part/gas_interface_configuration_terminal_off");
    public static final ResourceLocation MODEL_ON = new ResourceLocation(MekEng.MODID, "part/gas_interface_configuration_terminal_on");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public String in = "";

    public PartGasInterfaceConfigurationTerminal(final ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (!super.onPartActivate(player, hand, pos)) {
            if (Platform.isServer() && this.getActionableNode() != null) {
                GuiHandler.openPartGui(player, this.getTile().getWorld(), this.getTile().getPos(), this.getSide().getFacing(), MkEGuis.GAS_INTERFACE_TERMINAL);
            }
        }
        return true;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL);
    }

    public void saveSearchStrings(String in) {
        this.in = in;
    }

}
