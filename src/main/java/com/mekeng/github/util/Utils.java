package com.mekeng.github.util;

import appeng.container.interfaces.IInventorySlotAware;
import com.mekeng.github.common.container.handler.GuiFactory;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.inventory.IExtendedGasHandler;
import com.mekeng.github.util.helpers.ItemGasHandler;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import mekanism.api.gas.IGasItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class Utils {

    public static GasStack getGasFromItem(ItemStack stack) {
        IGasItem gh = getGasHandler(stack);
        if (gh != null) {
           return gh.getGas(stack);
        }
        return null;
    }

    public static ItemGasHandler getItemGasHandler(ItemStack stack) {
        IGasItem handler = getGasHandler(stack);
        if (handler != null) {
            return new ItemGasHandler(handler, stack);
        }
        return null;
    }

    public static IGasItem getGasHandler(ItemStack stack) {
        if (!stack.isEmpty() && stack.getItem() instanceof IGasItem) {
            return (IGasItem) stack.getItem();
        }
        return null;
    }

    public static String getGasModID(IAEGasStack stack) {
        return stack == null ? "** Null" : getGasModID(stack.getGasStack());
    }

    public static String getGasModID(GasStack stack) {
        if (stack != null) {
            Gas gas = stack.getGas();
            if (gas != null) {
                return gas.getIcon().getNamespace();
            }
        }
        return "** Null";
    }

    public static String getGasDisplayName(IAEGasStack stack) {
        return stack == null ? "** Null" : getGasDisplayName(stack.getGasStack());
    }

    public static String getGasDisplayName(GasStack stack) {
        if (stack != null) {
            Gas gas = stack.getGas();
            if (gas != null) {
                return gas.getLocalizedName();
            }
        }
        return "** Null";
    }

    public static GasStack drawGas(IGasHandler handler, GasStack stack, EnumFacing side, int amount, boolean doTransfer) {
        if (handler instanceof IExtendedGasHandler) {
            GasStack draw = stack.copy().withAmount(amount);
            return ((IExtendedGasHandler) handler).drawGas(side, draw, doTransfer);
        }
        return handler.drawGas(side, amount, doTransfer);
    }

    public static void openItemGui(EntityPlayer player, GuiFactory<?> gui) {
        if (player.openContainer instanceof IInventorySlotAware) {
            IInventorySlotAware c = (IInventorySlotAware) player.openContainer;
            GuiHandler.openItemGui(player, player.world, c.getInventorySlot(), c.isBaubleSlot(), gui);
        } else {
            GuiHandler.openItemGui(player, player.world, player.inventory.currentItem, false, gui);
        }
    }

}
