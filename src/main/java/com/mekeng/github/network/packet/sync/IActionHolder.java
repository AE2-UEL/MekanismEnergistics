package com.mekeng.github.network.packet.sync;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;

public interface IActionHolder {
    @Nonnull
    Map<String, Consumer<Paras>> getActionMap();

    default Map<String, Consumer<Paras>> createHolder() {
        return new Object2ObjectOpenHashMap<>();
    }

}
