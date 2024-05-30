package com.mekeng.github.network.packet;

import com.mekeng.github.MekEng;
import com.mekeng.github.client.gui.GuiGasTerminal;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SMEGasInventoryUpdate extends MkEMessage<SMEGasInventoryUpdate> {

    private final PacketBuffer buffer = new PacketBuffer(Unpooled.buffer(1024));
    private final List<IAEGasStack> list = new ArrayList<>();
    private int cnt = 0;

    public SMEGasInventoryUpdate() {
        // NO-OP
    }

    public SMEGasInventoryUpdate addGas(IAEGasStack stack) {
        try {
            stack.writeToPacket(buffer);
            cnt ++;
        } catch (IOException e) {
            buffer.clear();
            cnt = 0;
            MekEng.log.error(e);
        }
        return this;
    }

    public boolean needFlush() {
        return buffer.readableBytes() > 0xE00000;
    }

    public void clear() {
        buffer.clear();
        cnt = 0;
    }

    public boolean isEmpty() {
        return cnt == 0;
    }

    @Override
    void toBytes(PacketBuffer buf) {
        buf.writeInt(cnt);
        buf.writeBytes(buffer);
    }

    @Override
    void fromBytes(PacketBuffer buf) {
        cnt = buf.readInt();
        for (int i = 0; i < cnt; i ++) {
            list.add(AEGasStack.of(buf));
        }
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public IMessageHandler<SMEGasInventoryUpdate, IMessage> getHandler() {
        return (message, ctx) -> {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (Minecraft.getMinecraft().currentScreen instanceof GuiGasTerminal) {
                    ((GuiGasTerminal) Minecraft.getMinecraft().currentScreen).postUpdate(message.list);
                }
            });
            return null;
        };
    }

}
