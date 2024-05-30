package com.mekeng.github.common.me.inventory;

import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;

import javax.annotation.Nullable;

public interface IGasInventory extends Iterable<GasTank> {

    int size();

    boolean usable(int index);

    GasTank[] getTanks();

    @Nullable
    GasStack getGasStack(int index);

    int addGas(int index, GasStack stack, boolean simulate);

    GasStack removeGas(int index, GasStack stack, boolean simulate);

    GasStack removeGas(int index, int amount, boolean simulate);

    void setGas(int index, GasStack stack);

    void setCap(int cap);

}
