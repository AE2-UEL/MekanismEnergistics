package com.mekeng.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.util.Platform;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.common.tile.TileGasInterface;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockGasInterface extends AEBaseTileBlock {

    public BlockGasInterface() {
        super(Material.IRON);
        setTileEntity(TileGasInterface.class);
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p, final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side, final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }

        final TileEntity tg = this.getTileEntity(w, pos);
        if (tg instanceof TileGasInterface) {
            if (Platform.isServer()) {
                GuiHandler.openTileGui(p, w, pos, MkEGuis.GAS_INTERFACE);
            }
            return true;
        }
        return false;
    }
}