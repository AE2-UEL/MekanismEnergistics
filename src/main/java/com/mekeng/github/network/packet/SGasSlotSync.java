package com.mekeng.github.network.packet;

import com.mekeng.github.common.container.sync.IGasSyncContainer;
import com.mekeng.github.common.me.data.IAEGasStack;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.util.Map;

public class SGasSlotSync extends CGasSlotSync {

    public SGasSlotSync() {
        // NO-OP
    }

    public SGasSlotSync(Map<Integer, IAEGasStack> list) {
        super(list);
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public IMessageHandler<CGasSlotSync, IMessage> getHandler() {
        return (message, ctx) -> {
            EntityPlayer player = Minecraft.getMinecraft().player;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (player.openContainer instanceof IGasSyncContainer) {
                    IGasSyncContainer c = (IGasSyncContainer) player.openContainer;
                    c.receiveGasSlots(message.list);
                }
            });
            return null;
        };
    }

}
