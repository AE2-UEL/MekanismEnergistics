package com.mekeng.github.common.container.handler;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.helpers.WirelessTerminalGuiObject;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class GuiFactory<T> {

    protected final Class<T> invClass;
    private final int id;

    public GuiFactory(Class<T> invClass) {
        this.invClass = invClass;
        this.id = MkEGuis.registerFactory(this);
    }

    public int getId() {
        return this.id;
    }

    protected abstract Object createServerGui(EntityPlayer player, T inv);

    protected abstract Object createClientGui(EntityPlayer player, T inv);

    @Nullable
    protected T getInventory(@Nullable TileEntity tile, EntityPlayer player, EnumFacing face, int slot, boolean isBauble, GuiMode mode) {
        switch (mode) {
            case TILE:
                return invClass.isInstance(tile) ? invClass.cast(tile) : null;
            case PART:
                IPart part = null;
                if (tile instanceof IPartHost) {
                    part = ((IPartHost) tile).getPart(face);
                }
                return invClass.isInstance(part) ? invClass.cast(part) : null;
            case ITEM:
                ItemStack stack = player.inventory.getCurrentItem();
                Object guiObj = null;
                if (!stack.isEmpty()) {
                    guiObj = getItemGuiObject(stack, player, player.world, slot, isBauble);
                }
                return invClass.isInstance(guiObj) ? invClass.cast(guiObj) : null;
        }
        return null;
    }

    public Object createElement(EntityPlayer player, World world, int x, int y, int z, EnumFacing face, GuiMode mode, boolean isServer) {
        TileEntity tile = mode == GuiMode.ITEM ? null : world.getTileEntity(new BlockPos(x, y, z));
        int slot = 0;
        boolean isBauble = false;
        if (mode == GuiMode.ITEM) {
            slot = x;
            isBauble = y == 1;
        }
        T inv = getInventory(tile, player, face, slot, isBauble, mode);
        if (inv == null) {
            return null;
        }
        Object obj = isServer ? createServerGui(player, inv) : createClientGui(player, inv);
        if (obj instanceof AEBaseContainer) {
            ContainerOpenContext ctx = new ContainerOpenContext(inv);
            ctx.setWorld(world);
            ctx.setX(x);
            ctx.setY(y);
            ctx.setZ(z);
            if (mode == GuiMode.PART) {
                ctx.setSide(AEPartLocation.fromFacing(face));
            } else {
                ctx.setSide(AEPartLocation.INTERNAL);
            }
            ((AEBaseContainer) obj).setOpenContext(ctx);
        }
        return obj;
    }

    private static Object getItemGuiObject(ItemStack it, EntityPlayer player, World w, int slot, boolean isBauble) {
        if (!it.isEmpty()) {
            if (it.getItem() instanceof IGuiItem) {
                return ((IGuiItem)it.getItem()).getGuiObject(it, w, new BlockPos(slot, isBauble ? 1 : 0, 0));
            }
            IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WirelessTerminalGuiObject(wh, it, player, w, slot, isBauble ? 1 : 0, 0);
            }
        }
        return null;
    }

    public enum GuiMode {
        TILE,
        PART,
        ITEM
    }

}
