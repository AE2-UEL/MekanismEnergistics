package com.mekeng.github.common.me.storage;

import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import com.mekeng.github.common.me.data.IAEGasStack;

public interface IPortableGasCell extends ITerminalHost, IMEMonitor<IAEGasStack>, IEnergySource, IGuiItemObject {

}
