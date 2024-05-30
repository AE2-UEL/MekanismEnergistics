package com.mekeng.github.proxy;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.RegistryHandler;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.me.storage.impl.GasCellGuiHandler;
import com.mekeng.github.common.me.storage.impl.GasStorageChannel;
import com.mekeng.github.network.Packets;
import mekanism.common.MekanismBlocks;
import mekanism.common.MekanismItems;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class CommonProxy {

    public final RegistryHandler regHandler = createRegistryHandler();
    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(MekEng.MODID);

    public RegistryHandler createRegistryHandler() {
        return new RegistryHandler();
    }

    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(regHandler);
        ItemAndBlocks.init(regHandler);
        AEApi.instance().storage().registerStorageChannel(IGasStorageChannel.class, GasStorageChannel.INSTANCE);
        Packets.init();
    }

    public void init(FMLInitializationEvent event) {
        regHandler.onInit();
        AEApi.instance().registries().cell().addCellGuiHandler(GasCellGuiHandler.INSTANCE);
        AEApi.instance().registries().wireless().registerWirelessHandler(ItemAndBlocks.WIRELESS_GAS_TERMINAL);
    }

    public void postInit(FMLPostInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(MekEng.INSTANCE, GuiHandler.INSTANCE);
        this.loadP2P();
        this.loadUpgrades();
    }

    private void loadP2P() {
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismBlocks.GasTank), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.Flamethrower), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.GaugeDropper), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.Jetpack), MekEng.GAS);
        AEApi.instance().registries().p2pTunnel().addNewAttunement(new ItemStack(MekanismItems.ScubaTank), MekEng.GAS);
    }

    private void loadUpgrades() {
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_1k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_1k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_4k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_4k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_16k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_16k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_64k), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_CELL_64k), 1);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.PORTABLE_GAS_CELL), 1);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.PORTABLE_GAS_CELL), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 2);
        Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 1);
        Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlocks.GAS_IMPORT_BUS), 4);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 2);
        Upgrades.REDSTONE.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 1);
        Upgrades.SPEED.registerItem(new ItemStack(ItemAndBlocks.GAS_EXPORT_BUS), 4);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_INTERFACE), 2);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_INTERFACE_PART), 2);
        Upgrades.INVERTER.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 1);
        Upgrades.CAPACITY.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 5);
        Upgrades.STICKY.registerItem(new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS), 1);
        Upgrades.MAGNET.registerItem(new ItemStack(ItemAndBlocks.WIRELESS_GAS_TERMINAL), 1);
    }

}
