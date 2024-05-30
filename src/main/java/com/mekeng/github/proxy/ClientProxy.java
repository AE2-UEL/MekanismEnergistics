package com.mekeng.github.proxy;

import appeng.api.util.AEColor;
import com.mekeng.github.client.ClientRegistryHandler;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.RegistryHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public RegistryHandler createRegistryHandler() {
        return new ClientRegistryHandler();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), ItemAndBlocks.GAS_TERMINAL);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> AEColor.TRANSPARENT.getVariantByTintIndex(i), ItemAndBlocks.GAS_INTERFACE_TERMINAL);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((s, i) -> {
            GasStack gas = ItemAndBlocks.DUMMY_GAS.getGasStack(s);
            return gas != null ? gas.getGas().getTint() : 0xFFFFFFFF;
        }, ItemAndBlocks.DUMMY_GAS);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event){
        super.postInit(event);
    }

}