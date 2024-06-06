package com.mekeng.github.network.packet.sync;

import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public final class ParaSerializer {

    //////////////////////////////
    //                          //
    //     Serializer Zone      //
    //                          //
    //////////////////////////////

    public static void to(Object[] obj, ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        buffer.writeByte(obj.length);
        for (Object o : obj) {
            if (o == null) {
                buffer.writeByte(PT.VOID.ordinal());
            } else if (o instanceof Integer) {
                buffer.writeByte(PT.INT.ordinal());
                buffer.writeInt((int) o);
            } else if (o instanceof Long) {
                buffer.writeByte(PT.LONG.ordinal());
                buffer.writeLong((long) o);
            } else if (o instanceof Short) {
                buffer.writeByte(PT.SHORT.ordinal());
                buffer.writeShort((short) o);
            } else if (o instanceof Boolean) {
                buffer.writeByte(PT.BOOLEAN.ordinal());
                buffer.writeBoolean((boolean) o);
            } else if (o instanceof String) {
                buffer.writeByte(PT.STRING.ordinal());
                buffer.writeString((String) o);
            } else if (o instanceof ItemStack) {
                buffer.writeByte(PT.STACK.ordinal());
                buffer.writeItemStack((ItemStack) o);
            } else if (o instanceof NBTTagCompound) {
                buffer.writeByte(PT.NBT.ordinal());
                buffer.writeCompoundTag((NBTTagCompound) o);
            } else if (o instanceof IAEGasStack) {
                buffer.writeByte(PT.AE_GAS_STACK.ordinal());
                try {
                    ((IAEGasStack) o).writeToPacket(buf);
                } catch (IOException e) {
                    MekEng.log.debug(e);
                }
            } else {
                throw new IllegalArgumentException("Args contains invalid type: " + o.getClass().getName());
            }
        }
    }

    public static Object[] from(ByteBuf buf) {
        PacketBuffer buffer = new PacketBuffer(buf);
        Object[] objs = new Object[buffer.readByte()];
        try {
            for (int i = 0; i < objs.length; i ++) {
                switch (PT.values()[buffer.readByte()]) {
                    case VOID:
                        objs[i] = null;
                        break;
                    case INT :
                        objs[i] = buffer.readInt();
                        break;
                    case LONG :
                        objs[i] = buffer.readLong();
                        break;
                    case SHORT :
                        objs[i] = buffer.readShort();
                        break;
                    case BOOLEAN:
                        objs[i] = buffer.readBoolean();
                        break;
                    case STRING :
                        objs[i] = buffer.readString(1024);
                        break;
                    case STACK :
                        objs[i] = buffer.readItemStack();
                        break;
                    case NBT:
                        objs[i] = buffer.readCompoundTag();
                        break;
                    case AE_GAS_STACK:
                        objs[i] = AEGasStack.of(buffer);
                        break;
                    default :
                        throw new IllegalArgumentException("Args contains unknown type.");
                }
            }
        } catch (IOException e) {
            MekEng.log.debug(e);
        }
        return objs;
    }

    private enum PT {
        VOID,
        INT,
        LONG,
        SHORT,
        BOOLEAN,
        STRING,
        STACK,
        NBT,
        AE_GAS_STACK
    }

}