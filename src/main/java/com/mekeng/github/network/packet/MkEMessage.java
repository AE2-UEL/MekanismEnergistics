package com.mekeng.github.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

public abstract class MkEMessage<T extends IMessage> implements IMessage {

    public MkEMessage() {
        // NO-OP
    }

    public abstract boolean isClient();

    public abstract IMessageHandler<T, IMessage> getHandler();

    abstract void toBytes(PacketBuffer buf);

    abstract void fromBytes(PacketBuffer buf);

    @Override
    public final void toBytes(ByteBuf buf) {
        this.toBytes(new PacketBuffer(buf));
    }

    @Override
    public final void fromBytes(ByteBuf buf) {
        this.fromBytes(new PacketBuffer(buf));
    }

}
