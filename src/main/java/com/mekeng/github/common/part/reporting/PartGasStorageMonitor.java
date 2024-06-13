package com.mekeng.github.common.part.reporting;

import appeng.api.parts.IPartModel;
import appeng.core.AppEng;
import appeng.parts.PartModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class PartGasStorageMonitor extends AbstractPartGasMonitor {

    public static final ResourceLocation MODEL_OFF = new ResourceLocation(AppEng.MOD_ID, "part/storage_monitor_off");
    public static final ResourceLocation MODEL_ON = new ResourceLocation(AppEng.MOD_ID, "part/storage_monitor_on");
    public static final ResourceLocation MODEL_LOCKED_OFF = new ResourceLocation(AppEng.MOD_ID, "part/storage_monitor_locked_off");
    public static final ResourceLocation MODEL_LOCKED_ON = new ResourceLocation(AppEng.MOD_ID, "part/storage_monitor_locked_on");

    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, MODEL_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_ON, MODEL_STATUS_HAS_CHANNEL);

    public static final IPartModel MODELS_LOCKED_OFF = new PartModel(MODEL_BASE, MODEL_LOCKED_OFF, MODEL_STATUS_OFF);
    public static final IPartModel MODELS_LOCKED_ON = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_ON);
    public static final IPartModel MODELS_LOCKED_HAS_CHANNEL = new PartModel(MODEL_BASE, MODEL_LOCKED_ON, MODEL_STATUS_HAS_CHANNEL);

    public PartGasStorageMonitor(ItemStack is) {
        super(is);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return this.selectModel(MODELS_OFF, MODELS_ON, MODELS_HAS_CHANNEL, MODELS_LOCKED_OFF, MODELS_LOCKED_ON, MODELS_LOCKED_HAS_CHANNEL);
    }

}
