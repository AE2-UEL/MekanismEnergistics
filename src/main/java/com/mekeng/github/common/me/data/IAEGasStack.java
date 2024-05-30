package com.mekeng.github.common.me.data;

import appeng.api.storage.data.IAEStack;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;

public interface IAEGasStack extends IAEStack<IAEGasStack> {

    GasStack getGasStack();

    Gas getGas();

}
