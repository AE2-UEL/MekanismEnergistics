package com.mekeng.github.common.item;

import appeng.core.sync.GuiWrapper;
import appeng.items.tools.powered.ToolWirelessTerminal;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.container.handler.AEGuiBridge;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ItemWirelessGasTerminal extends ToolWirelessTerminal {

    @Override
    public boolean canHandle(final ItemStack is) {
        return is.getItem() == ItemAndBlocks.WIRELESS_GAS_TERMINAL;
    }

    @Override
    public IGuiHandler getGuiHandler(ItemStack is) {
        return GuiWrapper.INSTANCE.wrap(AEGuiBridge.WIRELESS_GAS_TERM);
    }

}
