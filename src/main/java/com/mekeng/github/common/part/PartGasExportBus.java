package com.mekeng.github.common.part;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SchedulingMode;
import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.GasTickRates;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PartGasExportBus extends PartSharedGasBus {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(MekEng.MODID, "part/gas_export_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_export_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_export_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_export_bus_has_channel"));

    private final IActionSource source;

    public PartGasExportBus(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_CONTROLLED, RedstoneMode.IGNORE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.CRAFT_ONLY, YesNo.NO);
        this.getConfigManager().registerSetting(Settings.SCHEDULING_MODE, SchedulingMode.DEFAULT);
        this.source = new MachineSource(this);
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(GasTickRates.GasExportBus.getMin(), GasTickRates.GasExportBus.getMax(), this.isSleeping(), false);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    @Override
    public boolean isExport() {
        return true;
    }

    @Override
    protected TickRateModulation doBusWork() {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        final TileEntity te = this.getConnectedTE();

        if (te != null && te.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite())) {
            try {
                final EnumFacing side = this.getSide().getFacing().getOpposite();
                final IGasHandler gh = te.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, side);
                final IMEMonitor<IAEGasStack> inv = this.getProxy().getStorage().getInventory(this.getChannel());

                if (gh != null) {
                    for (int i = 0; i < this.getConfig().size(); i++) {
                        IAEGasStack gas = AEGasStack.of(this.getConfig().getGasStack(i));
                        if (gas != null) {
                            final IAEGasStack toExtract = gas.copy();

                            toExtract.setStackSize(this.calculateAmountToSend());

                            final IAEGasStack out = inv.extractItems(toExtract, Actionable.SIMULATE, this.source);

                            if (out != null) {
                                int wasInserted = gh.receiveGas(side, out.getGasStack(), true);

                                if (wasInserted > 0) {
                                    toExtract.setStackSize(wasInserted);
                                    inv.extractItems(toExtract, Actionable.MODULATE, this.source);
                                    return TickRateModulation.FASTER;
                                }
                            }
                        }
                    }

                    return TickRateModulation.SLOWER;
                }
            } catch (GridAccessException e) {
                // Ignore
            }
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(4, 4, 12, 12, 12, 14);
        bch.addBox(5, 5, 14, 11, 11, 15);
        bch.addBox(6, 6, 15, 10, 10, 16);
        bch.addBox(6, 6, 11, 10, 10, 12);
    }

    @Override
    public RedstoneMode getRSMode() {
        return (RedstoneMode) this.getConfigManager().getSetting(Settings.REDSTONE_CONTROLLED);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

}
