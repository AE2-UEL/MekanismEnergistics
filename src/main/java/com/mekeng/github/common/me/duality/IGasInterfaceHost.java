package com.mekeng.github.common.me.duality;

import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.security.IActionHost;
import appeng.me.helpers.IGridProxyable;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.util.EnumSet;

public interface IGasInterfaceHost extends IActionHost, IGridProxyable, IUpgradeableHost {

    DualityGasInterface getDualityGasInterface();

    EnumSet<EnumFacing> getTargets();

    TileEntity getTileEntity();

    void saveChanges();

}
