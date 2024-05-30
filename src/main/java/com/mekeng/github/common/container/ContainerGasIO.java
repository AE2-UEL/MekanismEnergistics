package com.mekeng.github.common.container;

import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.part.PartSharedGasBus;
import net.minecraft.entity.player.InventoryPlayer;

public class ContainerGasIO extends ContainerGasConfigurable<PartSharedGasBus> {

    public ContainerGasIO(InventoryPlayer ip, PartSharedGasBus te) {
        super(ip, te);
    }

    @Override
    public IGasInventory getGasConfigInventory() {
        return this.getUpgradeable().getConfig();
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

}
