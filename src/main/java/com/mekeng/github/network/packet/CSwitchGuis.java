package com.mekeng.github.network.packet;

import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import com.mekeng.github.common.container.handler.GuiFactory;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.util.Ae2Reflect;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public class CSwitchGuis extends MkEMessage<CSwitchGuis> {

    private GuiFactory<?> factory;
    private GuiFactory.GuiMode mode;

    public CSwitchGuis(GuiFactory<?> guiType, GuiFactory.GuiMode mode) {
        this.factory = guiType;
        this.mode = mode;
    }

    public CSwitchGuis() {
        // NO-OP
    }

    @Override
    void fromBytes(PacketBuffer byteBuf) {
        factory = MkEGuis.getFactory(byteBuf.readByte());
        mode = GuiFactory.GuiMode.values()[byteBuf.readByte()];
    }

    @Override
    void toBytes(PacketBuffer byteBuf) {
        byteBuf.writeByte(factory != null ? factory.getId() : 0);
        byteBuf.writeByte(mode.ordinal());
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public IMessageHandler<CSwitchGuis, IMessage> getHandler() {
        return (message, ctx) -> {
            if (message.factory == null) {
                return null;
            }
            EntityPlayerMP player = ctx.getServerHandler().player;
            Container cont = player.openContainer;
            if (!(cont instanceof AEBaseContainer)) {
                return null;
            }
            ContainerOpenContext context = ((AEBaseContainer) cont).getOpenContext();
            if (context == null) {
                return null;
            }
            player.getServerWorld().addScheduledTask(
                    () -> GuiHandler.openGui(
                            player,
                            player.world,
                            Ae2Reflect.getContextX(context),
                            Ae2Reflect.getContextY(context),
                            Ae2Reflect.getContextZ(context),
                            message.mode,
                            context.getSide().getFacing(),
                            message.factory
                    )
            );
            return null;
        };
    }

}