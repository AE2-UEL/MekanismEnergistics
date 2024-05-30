package com.mekeng.github.network.packet;

import com.mekeng.github.network.packet.sync.IActionHolder;
import com.mekeng.github.network.packet.sync.ParaSerializer;
import com.mekeng.github.network.packet.sync.Paras;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.util.function.Consumer;

public class CGenericPacket extends MkEMessage<CGenericPacket> {

    private String name;
    private Object[] paras;

    public CGenericPacket() {
        // NO-OP
    }

    public CGenericPacket(String name) {
        this.name = name;
        this.paras = null;
    }

    public CGenericPacket(String name, Object... paras) {
        this.name = name;
        this.paras = paras;
    }

    @Override
    void toBytes(PacketBuffer buf) {
        buf.writeString(this.name);
        if (this.paras == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            ParaSerializer.to(this.paras, buf);
        }
    }

    @Override
    void fromBytes(PacketBuffer buf) {
        this.name = buf.readString(1024);
        if (buf.readBoolean()) {
            this.paras = ParaSerializer.from(buf);
        } else {
            this.paras = null;
        }
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public IMessageHandler<CGenericPacket, IMessage> getHandler() {
        return (message, ctx) -> {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof IActionHolder) {
                    IActionHolder ah = (IActionHolder) player.openContainer;
                    Consumer<Paras> fun = ah.getActionMap().get(message.name);
                    if (fun != null) {
                        fun.accept(new Paras(message.paras));
                    }
                }
            });
            return null;
        };
    }

}
