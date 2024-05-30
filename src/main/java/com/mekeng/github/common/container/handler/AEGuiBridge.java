package com.mekeng.github.common.container.handler;

import appeng.api.parts.IPartHost;
import appeng.core.sync.GuiWrapper;
import com.mekeng.github.MekEng;
import com.mekeng.github.util.Utils;
import net.minecraft.util.ResourceLocation;

public class AEGuiBridge implements GuiWrapper.IExternalGui {

    public static AEGuiBridge GAS_INTERFACE = new AEGuiBridge(MkEGuis.GAS_INTERFACE, "gas_interface");
    public static AEGuiBridge GAS_STORAGE_BUS = new AEGuiBridge(MkEGuis.GAS_STORAGE_BUS, "gas_storage_bus");
    public static AEGuiBridge GAS_TERMINAL = new AEGuiBridge(MkEGuis.GAS_TERMINAL, "gas_terminal");
    public static AEGuiBridge WIRELESS_GAS_TERM = new AEGuiBridge(MkEGuis.WIRELESS_GAS_TERM, "wireless_gas_terminal");

    final ResourceLocation id;
    final GuiFactory<?> obj;

    public AEGuiBridge(GuiFactory<?> factory, String key) {
        this.id = MekEng.id(key);
        this.obj = factory;
        GuiWrapper.INSTANCE.registerExternalGuiHandler(this.id, this::openGui);
    }

    private void openGui(GuiWrapper.IExternalGui gui, GuiWrapper.GuiContext ctx) {
        if (gui instanceof AEGuiBridge) {
            GuiFactory<?> factory = ((AEGuiBridge) gui).obj;
            if (ctx.pos != null) {
                if (ctx.facing != null) {
                    if (ctx.world.getTileEntity(ctx.pos) instanceof IPartHost) {
                        GuiHandler.openPartGui(ctx.player, ctx.world, ctx.pos, ctx.facing, factory);
                    } else {
                        GuiHandler.openTileGui(ctx.player, ctx.world, ctx.pos, factory);
                    }
                } else {
                    GuiHandler.openTileGui(ctx.player, ctx.world, ctx.pos, factory);
                }
            } else if (ctx.extra != null) {
                int slot = ctx.extra.getInteger("slot");
                boolean isBauble = ctx.extra.getBoolean("isBauble");
                GuiHandler.openItemGui(ctx.player, ctx.world, slot, isBauble, factory);
            } else {
                Utils.openItemGui(ctx.player, factory);
            }
        }
    }

    @Override
    public ResourceLocation getID() {
        return this.id;
    }

}
