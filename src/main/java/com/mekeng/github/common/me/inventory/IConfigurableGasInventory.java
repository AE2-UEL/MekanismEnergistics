package com.mekeng.github.common.me.inventory;

public interface IConfigurableGasInventory {

    default IGasInventory getGasInventoryByName(String name) {
        return null;
    }

}
