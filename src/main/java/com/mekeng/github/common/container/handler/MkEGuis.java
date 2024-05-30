package com.mekeng.github.common.container.handler;

import appeng.api.storage.ITerminalHost;
import appeng.helpers.WirelessTerminalGuiObject;
import com.mekeng.github.client.gui.GuiGasIO;
import com.mekeng.github.client.gui.GuiGasInterface;
import com.mekeng.github.client.gui.GuiGasInterfaceConfigurationTerminal;
import com.mekeng.github.client.gui.GuiGasLevelEmitter;
import com.mekeng.github.client.gui.GuiGasStorageBus;
import com.mekeng.github.client.gui.GuiGasTerminal;
import com.mekeng.github.client.gui.GuiMEPortableGasCell;
import com.mekeng.github.client.gui.GuiWirelessGasTerminal;
import com.mekeng.github.common.container.ContainerGasIO;
import com.mekeng.github.common.container.ContainerGasInterface;
import com.mekeng.github.common.container.ContainerGasInterfaceConfigurationTerminal;
import com.mekeng.github.common.container.ContainerGasLevelEmitter;
import com.mekeng.github.common.container.ContainerGasStorageBus;
import com.mekeng.github.common.container.ContainerGasTerminal;
import com.mekeng.github.common.container.ContainerMEPortableGasCell;
import com.mekeng.github.common.container.ContainerWirelessGasTerminal;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.storage.IPortableGasCell;
import com.mekeng.github.common.part.PartGasInterfaceConfigurationTerminal;
import com.mekeng.github.common.part.PartGasLevelEmitter;
import com.mekeng.github.common.part.PartGasStorageBus;
import com.mekeng.github.common.part.PartSharedGasBus;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;

public class MkEGuis {

    private static final Int2ObjectMap<GuiFactory<?>> GUIS = new Int2ObjectOpenHashMap<>();
    private static int nextID = 1;

    public static GuiFactory<ITerminalHost> GAS_TERMINAL = new GuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerGasTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiGasTerminal(player.inventory, inv);
        }
    };

    public static GuiFactory<PartSharedGasBus> GAS_IO_BUS = new GuiFactory<PartSharedGasBus>(PartSharedGasBus.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartSharedGasBus inv) {
            return new ContainerGasIO(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartSharedGasBus inv) {
            return new GuiGasIO(player.inventory, inv);
        }
    };

    public static GuiFactory<IGasInterfaceHost> GAS_INTERFACE = new GuiFactory<IGasInterfaceHost>(IGasInterfaceHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IGasInterfaceHost inv) {
            return new ContainerGasInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IGasInterfaceHost inv) {
            return new GuiGasInterface(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasStorageBus> GAS_STORAGE_BUS = new GuiFactory<PartGasStorageBus>(PartGasStorageBus.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasStorageBus inv) {
            return new ContainerGasStorageBus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasStorageBus inv) {
            return new GuiGasStorageBus(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasLevelEmitter> GAS_LEVEL_EMITTER = new GuiFactory<PartGasLevelEmitter>(PartGasLevelEmitter.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasLevelEmitter inv) {
            return new ContainerGasLevelEmitter(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasLevelEmitter inv) {
            return new GuiGasLevelEmitter(player.inventory, inv);
        }
    };

    public static GuiFactory<PartGasInterfaceConfigurationTerminal> GAS_INTERFACE_TERMINAL = new GuiFactory<PartGasInterfaceConfigurationTerminal>(PartGasInterfaceConfigurationTerminal.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartGasInterfaceConfigurationTerminal inv) {
            return new ContainerGasInterfaceConfigurationTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartGasInterfaceConfigurationTerminal inv) {
            return new GuiGasInterfaceConfigurationTerminal(player.inventory, inv);
        }
    };

    public static GuiFactory<IPortableGasCell> PORTABLE_GAS_CELL = new GuiFactory<IPortableGasCell>(IPortableGasCell.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IPortableGasCell inv) {
            return new ContainerMEPortableGasCell(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IPortableGasCell inv) {
            return new GuiMEPortableGasCell(player.inventory, inv);
        }
    };

    public static GuiFactory<WirelessTerminalGuiObject> WIRELESS_GAS_TERM = new GuiFactory<WirelessTerminalGuiObject>(WirelessTerminalGuiObject.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new ContainerWirelessGasTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new GuiWirelessGasTerminal(player.inventory, inv);
        }
    };

    protected static synchronized int registerFactory(GuiFactory<?> factory) {
        GUIS.put(nextID, factory);
        int id = nextID;
        nextID ++;
        return id;
    }

    public static GuiFactory<?> getFactory(int id) {
        return GUIS.get(id);
    }

}
