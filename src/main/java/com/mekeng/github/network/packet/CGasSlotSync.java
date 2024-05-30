package com.mekeng.github.network.packet;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.container.sync.IGasSyncContainer;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;

import java.io.IOException;
import java.util.Map;

public class CGasSlotSync extends MkEMessage<CGasSlotSync> {

    protected Map<Integer, IAEGasStack> list;

    public CGasSlotSync() {
        // NO-OP
    }

    public CGasSlotSync(Map<Integer, IAEGasStack> list) {
        this.list = list;
    }

    @Override
    void toBytes(PacketBuffer buf) {
        NBTTagCompound HOLDER = new NBTTagCompound();
        for (Map.Entry<Integer, IAEGasStack> data : this.list.entrySet()) {
            NBTTagCompound gas = new NBTTagCompound();
            if (data.getValue() != null) {
                data.getValue().writeToNBT(gas);
            }
            HOLDER.setTag(data.getKey().toString(), gas);
        }
        ByteBufUtils.writeTag(buf, HOLDER);
    }

    @Override
    void fromBytes(PacketBuffer buf) {
        this.list = new Int2ObjectOpenHashMap<>();
        try {
            NBTTagCompound HOLDER = buf.readCompoundTag();
            if (HOLDER != null) {
                for (String key : HOLDER.getKeySet()) {
                    int id = Integer.parseInt(key);
                    IAEGasStack gas = AEGasStack.of(HOLDER.getCompoundTag(key));
                    this.list.put(id, gas);
                }
            }
        } catch (IOException e) {
            MekEng.log.error("Fail to sync gas slot, it may cause ghost gas display");
            MekEng.log.error(e);
        }
    }

    @Override
    public boolean isClient() {
        return false;
    }

    @Override
    public IMessageHandler<CGasSlotSync, IMessage> getHandler() {
        return (message, ctx) -> {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.getServerWorld().addScheduledTask(() -> {
                if (player.openContainer instanceof IGasSyncContainer) {
                    IGasSyncContainer c = (IGasSyncContainer) player.openContainer;
                    c.receiveGasSlots(message.list);
                }
            });
            return null;
        };
    }

}
