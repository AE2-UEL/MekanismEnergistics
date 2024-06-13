package com.mekeng.github.common.part.reporting;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.fluids.util.AEFluidStack;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.helpers.PlayerSource;
import appeng.parts.PartModel;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.ItemGasHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class PartGasConversionMonitor extends AbstractPartMonitor {
    @PartModels
    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/conversion_monitor_off");
    @PartModels
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/conversion_monitor_on");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_OFF = new ResourceLocation(AppEng.MOD_ID, "part/conversion_monitor_locked_off");
    @PartModels
    public static final ResourceLocation MODEL_LOCKED_ON = new ResourceLocation(AppEng.MOD_ID, "part/conversion_monitor_locked_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);
    public static final IPartModel MODELS_LOCKED_OFF = new PartModel(MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_LOCKED_ON = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_HAS_CHANNEL);
    public PartGasConversionMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        final ItemStack eq = player.getHeldItem(hand);
        GasStack gasInTank = null;
        ItemGasHandler itemGasHandler = Utils.getItemGasHandler(eq);
        if (itemGasHandler != null) {
            gasInTank = itemGasHandler.removeGas(Integer.MAX_VALUE,false);
        }

        if (this.isLocked()) {
            if (Platform.isWrench(player, eq, this.getLocation().getPos()) && (this.getDisplayed() == null || !this.getDisplayed().equals(eq))) {
                // wrench it
                return super.onPartActivate(player, hand, pos);
            } else if (gasInTank != null && gasInTank.amount > 0) {
                if (this.getDisplayed() != null && getDisplayed().equals(AEGasStack.of(gasInTank))) {
                    this.drainGasContainer(player, hand);
                }
            }
        }

        return super.onPartActivate(player, hand, pos);
    }

    @Override
    public boolean onClicked(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        if (this.getDisplayed() != null && this.getDisplayed() instanceof IAEGasStack) {
            this.fillGasContainer(player, hand);
        }

        return true;
    }

    private void drainGasContainer(final EntityPlayer player, final EnumHand hand) {
        try {
            final ItemStack held = player.getHeldItem(hand);
            if (held.getCount() != 1) {
                // only support stacksize 1 for now
                return;
            }

            final ItemGasHandler itemGasHandler = Utils.getItemGasHandler(held);
            if (itemGasHandler == null) {
                // only fluid handlers items
                return;
            }

            // See how much we can drain from the item
            final GasStack extract = itemGasHandler.removeGas(Integer.MAX_VALUE, false);
            if (extract == null || extract.amount < 1) {
                return;
            }

            // Check if we can push into the system
            final IEnergySource energy = this.getProxy().getEnergy();
            final IMEMonitor<IAEGasStack> cell = this.getProxy()
                    .getStorage()
                    .getInventory(
                            AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            final IAEGasStack notStorable = Platform.poweredInsert(energy, cell, AEGasStack.of(extract), new PlayerSource(player, this), Actionable.SIMULATE);

            if (notStorable != null && notStorable.getStackSize() > 0) {
                final int toStore = (int) (extract.amount - notStorable.getStackSize());
                final GasStack storable = itemGasHandler.removeGas(toStore, false);

                if (storable == null || storable.amount == 0) {
                    return;
                } else {
                    extract.amount = storable.amount;
                }
            }

            // Actually drain
            final GasStack drained = itemGasHandler.removeGas(extract, true);
            extract.amount = drained.amount;

            final IAEGasStack notInserted = Platform.poweredInsert(energy, cell, AEGasStack.of(extract), new PlayerSource(player, this));

            if (notInserted != null && notInserted.getStackSize() > 0) {
                AELog.error("Fluid item [%s] reported a different possible amount to drain than it actually provided.", held.getDisplayName());
            }

            player.setHeldItem(hand, itemGasHandler.getContainer());
        } catch (GridAccessException e) {
            e.printStackTrace();
        }
    }

    private void fillGasContainer(final EntityPlayer player, final EnumHand hand) {
        try {
            final ItemStack held = player.getHeldItem(hand);
            if (held.getCount() != 1) {
                // only support stacksize 1 for now
                return;
            }

            final ItemGasHandler itemGasHandler = Utils.getItemGasHandler(held);
            if (itemGasHandler == null) {
                // only fluid handlers items
                return;
            }

            final IAEGasStack stack = (IAEGasStack) this.getDisplayed().copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = itemGasHandler.addGas(stack.getGasStack(), false);
            stack.setStackSize(amountAllowed);

            // Check if we can pull out of the system
            final IEnergySource energy = this.getProxy().getEnergy();
            final IMEMonitor<IAEGasStack> cell = this.getProxy()
                    .getStorage()
                    .getInventory(
                            AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            final IAEGasStack canPull = Platform.poweredExtraction(energy, cell, stack, new PlayerSource(player, this), Actionable.SIMULATE);
            if (canPull == null || canPull.getStackSize() < 1) {
                return;
            }

            // How much could fit into the container
            final int canFill = itemGasHandler.addGas(canPull.getGasStack(), false);
            if (canFill == 0) {
                return;
            }

            // Now actually pull out of the system
            stack.setStackSize(canFill);
            final IAEGasStack pulled = Platform.poweredExtraction(energy, cell, stack, new PlayerSource(player, this));
            if (pulled == null || pulled.getStackSize() < 1) {
                // Something went wrong
                AELog.error("Unable to pull fluid out of the ME system even though the simulation said yes ");
                return;
            }

            // Actually fill
            final int used = itemGasHandler.addGas(pulled.getGasStack(), true);

            if (used != canFill) {
                AELog.error("Fluid item [%s] reported a different possible amount than it actually accepted.", held.getDisplayName());
            }
            player.setHeldItem(hand, itemGasHandler.getContainer());
        } catch (GridAccessException e) {
            e.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL,
                MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL);
    }

    public static Collection<ResourceLocation> getModels() {
        Set<ResourceLocation> all = new HashSet<>();
        all.add(MODEL_ON);
        all.add(MODEL_LOCKED_ON);
        all.add(MODEL_LOCKED_OFF);
        all.add(MODEL_OFF);
        return all;
    }
}
