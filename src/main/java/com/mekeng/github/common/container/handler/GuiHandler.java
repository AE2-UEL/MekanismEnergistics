package com.mekeng.github.common.container.handler;

import com.mekeng.github.MekEng;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final GuiHandler INSTANCE = new GuiHandler();

    private GuiHandler() {
        // NO-OP
    }

    public static void openGui(EntityPlayer player, World world, int x, int y, int z, GuiFactory.GuiMode mode, EnumFacing face, GuiFactory<?> gui) {
        player.openGui(MekEng.INSTANCE, (gui.getId() << 5) | (mode.ordinal() << 3) | (face.getIndex()), world, x, y, z);
    }

    public static void openTileGui(EntityPlayer player, World world, BlockPos pos, GuiFactory<?> gui) {
        player.openGui(MekEng.INSTANCE, gui.getId() << 5, world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void openPartGui(EntityPlayer player, World world, BlockPos pos, EnumFacing face, GuiFactory<?> gui) {
        player.openGui(MekEng.INSTANCE, (gui.getId() << 5) | (1 << 3) | (face.getIndex()), world, pos.getX(), pos.getY(), pos.getZ());
    }

    public static void openItemGui(EntityPlayer player, World world, int slot, boolean isBauble, GuiFactory<?> gui) {
        player.openGui(MekEng.INSTANCE, (gui.getId() << 5) | (2 << 3), world, slot, isBauble ? 1 : 0, 0);
    }

    /**
     * ID structure: <br>
     * xxxx xxxx xxxy yzzz <br>
     * x: Gui ID <br>
     * y: Gui mode <br>
     * z: facing
     */
    @Nullable
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiFactory<?> gui = MkEGuis.getFactory(ID >>> 5);
        GuiFactory.GuiMode mode = GuiFactory.GuiMode.values()[(ID >>> 3) & 0b11];
        EnumFacing face = EnumFacing.byIndex(ID & 0b111);
        return gui != null ? gui.createElement(player, world, x, y, z, face, mode, true) : null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        GuiFactory<?> gui = MkEGuis.getFactory(ID >>> 5);
        GuiFactory.GuiMode mode = GuiFactory.GuiMode.values()[(ID >>> 3) & 0b11];
        EnumFacing face = EnumFacing.byIndex(ID & 0b111);
        return gui != null ? gui.createElement(player, world, x, y, z, face, mode, false) : null;
    }

}
