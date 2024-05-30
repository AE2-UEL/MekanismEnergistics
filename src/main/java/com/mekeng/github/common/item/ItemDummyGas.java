package com.mekeng.github.common.item;

import appeng.items.AEBaseItem;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.model.SpecialModel;
import mekanism.api.gas.GasStack;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import javax.annotation.Nonnull;

public class ItemDummyGas extends AEBaseItem implements SpecialModel {

    public ItemDummyGas() {
        setMaxStackSize(1);
    }

    @SuppressWarnings("deprecation")
    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        GasStack gasStack = this.getGasStack(stack);
        if (gasStack == null) {
            return I18n.translateToLocal("item.mekeng:dummy_gas.error.name");
        }
        return gasStack.getGas().getLocalizedName();
    }

    public GasStack getGasStack(ItemStack is) {
        if (is.hasTagCompound()) {
            NBTTagCompound tag = is.getTagCompound();
            return GasStack.readFromNBT(tag);
        } else {
            return null;
        }
    }

    public void setGasStack(ItemStack is, GasStack gs) {
        if (gs == null) {
            is.setTagCompound(null);
        } else {
            NBTTagCompound tag = new NBTTagCompound();
            gs.write(tag);
            is.setTagCompound(tag);
        }
    }

    @Override
    public void getCheckedSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        // NO-OP
    }

    @Override
    public ResourceLocation getModelPath() {
        return MekEng.id("model/dummy_gas");
    }

}
