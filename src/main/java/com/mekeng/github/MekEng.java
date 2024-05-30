package com.mekeng.github;

import appeng.api.config.TunnelType;
import com.mekeng.github.proxy.CommonProxy;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = MekEng.MODID, useMetadata = true, dependencies = "required-after:appliedenergistics2@[v0.56.5,);required-after:mekanism")
public class MekEng {

    public static final TunnelType GAS;

    static {
        // add P2P type
        GAS = EnumHelper.addEnum(TunnelType.class, "GAS", new Class[0]);
    }

    public static final String MODID = "mekeng";

    @Mod.Instance(MODID)
    public static MekEng INSTANCE;

    @SidedProxy(clientSide = "com.mekeng.github.proxy.ClientProxy", serverSide = "com.mekeng.github.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Logger log;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        log = event.getModLog();
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MODID, path);
    }

}
