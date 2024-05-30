package com.mekeng.github.common.me.storage.impl;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellGuiHandler;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class GasCellGuiHandler implements ICellGuiHandler {

    public static GasCellGuiHandler INSTANCE = new GasCellGuiHandler();

    private GasCellGuiHandler() {
        // NO-OP
    }

    @Override
    public <T extends IAEStack<T>> boolean isHandlerFor(final IStorageChannel<T> channel) {
        return channel == AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public void openChestGui(final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler, final IMEInventoryHandler inv, final ItemStack is, final IStorageChannel chan) {
        GuiHandler.openTileGui(player, ((TileEntity) chest).getWorld(), ((TileEntity) chest).getPos(), MkEGuis.GAS_TERMINAL);
    }
}
