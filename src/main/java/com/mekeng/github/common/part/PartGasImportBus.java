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
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.me.GridAccessException;
import appeng.me.helpers.MachineSource;
import appeng.parts.PartModel;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.GasTickRates;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.util.Utils;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PartGasImportBus extends PartSharedGasBus {

    public static final ResourceLocation MODEL_BASE = new ResourceLocation(MekEng.MODID, "part/gas_import_bus_base");
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_import_bus_off"));
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_import_bus_on"));
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_import_bus_has_channel"));

    private final IActionSource source;

    public PartGasImportBus(ItemStack is) {
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
        return new TickingRequest(GasTickRates.GasImportBus.getMin(), GasTickRates.GasImportBus.getMax(), this.isSleeping(), false);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        return this.canDoBusWork() ? this.doBusWork() : TickRateModulation.IDLE;
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
                    final GasStack gasStack = gh.drawGas(side, this.calculateAmountToSend(), false);

                    if (this.filterEnabled() && !this.isInFilter(gasStack)) {
                        return TickRateModulation.SLOWER;
                    }

                    final AEGasStack aeGasStack = AEGasStack.of(gasStack);

                    if (aeGasStack != null) {
                        final IAEGasStack notInserted = inv.injectItems(aeGasStack, Actionable.MODULATE, this.source);

                        if (notInserted != null && notInserted.getStackSize() > 0) {
                            aeGasStack.decStackSize(notInserted.getStackSize());
                        }

                        Utils.drawGas(gh, aeGasStack.getGasStack(), side, aeGasStack.getGasStack().amount, true);

                        return TickRateModulation.FASTER;
                    }

                    return TickRateModulation.IDLE;
                }
            } catch (GridAccessException e) {
                MekEng.log.error(e);
            }
        }

        return TickRateModulation.SLEEP;
    }

    @Override
    protected boolean canDoBusWork() {
        return this.getProxy().isActive();
    }

    @Override
    public boolean isExport() {
        return false;
    }

    private boolean isInFilter(GasStack gas) {
        if (gas == null) {
            return false;
        }
        for (int i = 0; i < this.getConfig().size(); i++) {
            final GasStack filter = this.getConfig().getGasStack(i);
            if (gas.isGasEqual(filter)) {
                return true;
            }
        }
        return false;
    }

    private boolean filterEnabled() {
        for (int i = 0; i < this.getConfig().size(); i++) {
            if (this.getConfig().getGasStack(i) != null) {
                return true;
            }
        }
        return false;
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
