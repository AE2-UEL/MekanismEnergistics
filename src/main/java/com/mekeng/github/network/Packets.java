package com.mekeng.github.network;

import com.mekeng.github.MekEng;
import com.mekeng.github.network.packet.CGasSlotSync;
import com.mekeng.github.network.packet.CGenericPacket;
import com.mekeng.github.network.packet.CSwitchGuis;
import com.mekeng.github.network.packet.MkEMessage;
import com.mekeng.github.network.packet.SGasSlotSync;
import com.mekeng.github.network.packet.SGenericPacket;
import com.mekeng.github.network.packet.SMEGasInventoryUpdate;
import net.minecraftforge.fml.relauncher.Side;

public class Packets {

    private static int nextID = 1;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void register(MkEMessage packet) {
        MekEng.proxy.netHandler.registerMessage(packet.getHandler(), packet.getClass(), nextID++, packet.isClient() ? Side.CLIENT : Side.SERVER);
    }

    public static void init() {
        register(new CGenericPacket());
        register(new SGenericPacket());
        register(new CSwitchGuis());
        register(new SMEGasInventoryUpdate());
        register(new CGasSlotSync());
        register(new SGasSlotSync());
    }

}
