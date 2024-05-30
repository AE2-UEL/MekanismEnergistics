package com.mekeng.github.common.container.sync;

import com.mekeng.github.common.me.data.IAEGasStack;

import java.util.Map;

public interface IGasSyncContainer {

    void receiveGasSlots(final Map<Integer, IAEGasStack> gases);

}
