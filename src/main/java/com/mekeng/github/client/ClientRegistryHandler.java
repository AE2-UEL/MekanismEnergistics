package com.mekeng.github.client;

import appeng.api.AEApi;
import com.mekeng.github.MekEng;
import com.mekeng.github.client.model.SpecialModel;
import com.mekeng.github.client.render.DummyGasModel;
import com.mekeng.github.common.RegistryHandler;
import com.mekeng.github.common.part.PartGasExportBus;
import com.mekeng.github.common.part.PartGasImportBus;
import com.mekeng.github.common.part.PartGasInterface;
import com.mekeng.github.common.part.PartGasInterfaceConfigurationTerminal;
import com.mekeng.github.common.part.PartGasStorageBus;
import com.mekeng.github.common.part.PartGasTerminal;
import com.mekeng.github.common.part.p2p.PartP2PGases;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

public class ClientRegistryHandler extends RegistryHandler {

    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent event) {
        ModelLoaderRegistry.registerLoader(new DummyGasModel.Loader());
        for (Pair<String, Block> entry : blocks) {
            registerModel(entry.getLeft(), Item.getItemFromBlock(entry.getRight()));
        }
        for (Pair<String, Item> entry : items) {
            registerModel(entry.getLeft(), entry.getRight());
        }
        AEApi.instance().registries().partModels().registerModels(PartGasTerminal.MODEL_ON);
        AEApi.instance().registries().partModels().registerModels(PartGasTerminal.MODEL_OFF);
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasImportBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasExportBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterface.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_ON.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_OFF.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasStorageBus.MODELS_HAS_CHANNEL.getModels());
        AEApi.instance().registries().partModels().registerModels(PartGasInterfaceConfigurationTerminal.MODEL_ON);
        AEApi.instance().registries().partModels().registerModels(PartGasInterfaceConfigurationTerminal.MODEL_OFF);
        AEApi.instance().registries().partModels().registerModels(PartP2PGases.getModels());
    }

    private static void registerModel(String key, Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item instanceof SpecialModel ? ((SpecialModel) item).getModelPath() : MekEng.id(key), "inventory"));
    }

}
