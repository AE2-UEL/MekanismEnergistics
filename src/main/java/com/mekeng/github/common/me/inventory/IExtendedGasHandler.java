package com.mekeng.github.common.me.inventory;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.util.EnumFacing;

public interface IExtendedGasHandler extends IGasHandler {

    GasStack drawGas(EnumFacing side, GasStack stack, boolean doTransfer);

}
