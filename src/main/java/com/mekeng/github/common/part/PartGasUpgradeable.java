package com.mekeng.github.common.part;

import appeng.parts.automation.PartUpgradeable;
import appeng.util.SettingsFrom;
import com.mekeng.github.common.me.inventory.IConfigurableGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class PartGasUpgradeable extends PartUpgradeable {

    public PartGasUpgradeable(ItemStack is) {
        super(is);
    }

    @Override
    public void uploadSettings(final SettingsFrom from, final NBTTagCompound compound, EntityPlayer player) {
        super.uploadSettings(from, compound, player);
        if (this instanceof IConfigurableGasInventory) {
            final IGasInventory tank = ((IConfigurableGasInventory) this).getGasInventoryByName("config");
            if (tank instanceof GasInventory) {
                final GasInventory target = (GasInventory) tank;
                final GasInventory tmp = new GasInventory(target.size());
                tmp.load(compound.getCompoundTag("config"));
                for (int x = 0; x < tmp.size(); x++) {
                    target.setGas(x, tmp.getGasStack(x));
                }
            }
            if (this instanceof PartGasLevelEmitter) {
                final PartGasLevelEmitter partGasLevelEmitter = (PartGasLevelEmitter) this;
                partGasLevelEmitter.setReportingValue(compound.getLong("reportingValue"));
            }
        }
    }

    @Override
    protected NBTTagCompound downloadSettings(final SettingsFrom from) {
        NBTTagCompound output = super.downloadSettings(from);
        if (output == null) {
            output = new NBTTagCompound();
        }
        if (this instanceof IConfigurableGasInventory) {
            final IGasInventory tank = ((IConfigurableGasInventory) this).getGasInventoryByName("config");
            if (tank instanceof GasInventory) {
                output.setTag("config", ((GasInventory) tank).save());
            }
            if (this instanceof PartGasLevelEmitter) {
                final PartGasLevelEmitter partGasLevelEmitter = (PartGasLevelEmitter) this;
                output.setLong("reportingValue", partGasLevelEmitter.getReportingValue());
            }
        }
        return output.isEmpty() ? null : output;
    }

}
