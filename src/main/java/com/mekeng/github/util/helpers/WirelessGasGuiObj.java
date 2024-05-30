package com.mekeng.github.util.helpers;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.features.ILocatable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.IUpgradeableCellHost;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IMachineSet;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.me.cluster.implementations.QuantumCluster;
import appeng.parts.automation.StackUpgradeInventory;
import appeng.parts.automation.UpgradeInventory;
import appeng.tile.networking.TileWireless;
import appeng.tile.qnb.TileQuantumBridge;
import appeng.util.inv.IAEAppEngInventory;
import appeng.util.inv.InvOperation;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.common.me.storage.IPortableGasCell;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class WirelessGasGuiObj implements IPortableGasCell, IActionHost, IInventorySlotAware, IAEAppEngInventory, IUpgradeableCellHost {

    private final ItemStack effectiveItem;
    private final IWirelessTermHandler wth;
    private final String encryptionKey;
    private final EntityPlayer myPlayer;
    private final boolean isBaubleSlot;
    private IGrid targetGrid;
    private IStorageGrid sg;
    private IMEMonitor<IAEGasStack> gasStorage;
    private IWirelessAccessPoint myWap;
    private double sqRange = Double.MAX_VALUE;
    private double myRange = Double.MAX_VALUE;
    private final int inventorySlot;

    private final UpgradeInventory upgrades;
    private QuantumCluster myQC;

    public WirelessGasGuiObj(final IWirelessTermHandler wh, final ItemStack is, final EntityPlayer ep, final World w, final int x, final int y, final int z) {
        this.encryptionKey = wh.getEncryptionKey(is);
        this.effectiveItem = is;
        this.myPlayer = ep;
        this.wth = wh;
        this.inventorySlot = x;
        this.isBaubleSlot = y == 1;

        ILocatable obj = null;

        try {
            final long encKey = Long.parseLong(this.encryptionKey);
            obj = AEApi.instance().registries().locatable().getLocatableBy(encKey);
        } catch (final NumberFormatException err) {
            // :P
        }

        if (obj instanceof IActionHost) {
            final IGridNode n = ((IActionHost) obj).getActionableNode();
            if (n != null) {
                this.targetGrid = n.getGrid();
                if (this.targetGrid != null) {
                    this.sg = this.targetGrid.getCache(IStorageGrid.class);
                    if (this.sg != null) {
                        this.gasStorage = this.sg.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
                    }
                }
            }
        }

        upgrades = new StackUpgradeInventory(effectiveItem, this, 2);

        this.loadFromNBT();
    }

    public double getRange() {
        return this.myRange;
    }

    @Override
    public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
        return this.sg.getInventory(channel);
    }

    @Override
    public void addListener(final IMEMonitorHandlerReceiver<IAEGasStack> l, final Object verificationToken) {
        if (this.gasStorage != null) {
            this.gasStorage.addListener(l, verificationToken);
        }
    }

    @Override
    public void removeListener(final IMEMonitorHandlerReceiver<IAEGasStack> l) {
        if (this.gasStorage != null) {
            this.gasStorage.removeListener(l);
        }
    }

    @Override
    public IItemList<IAEGasStack> getAvailableItems(final IItemList<IAEGasStack> out) {
        if (this.gasStorage != null) {
            return this.gasStorage.getAvailableItems(out);
        }
        return out;
    }

    @Override
    public IItemList<IAEGasStack> getStorageList() {
        if (this.gasStorage != null) {
            return this.gasStorage.getStorageList();
        }
        return null;
    }

    @Override
    public AccessRestriction getAccess() {
        if (this.gasStorage != null) {
            return this.gasStorage.getAccess();
        }
        return AccessRestriction.NO_ACCESS;
    }

    @Override
    public boolean isPrioritized(final IAEGasStack input) {
        if (this.gasStorage != null) {
            return this.gasStorage.isPrioritized(input);
        }
        return false;
    }

    @Override
    public boolean canAccept(final IAEGasStack input) {
        if (this.gasStorage != null) {
            return this.gasStorage.canAccept(input);
        }
        return false;
    }

    @Override
    public int getPriority() {
        if (this.gasStorage != null) {
            return this.gasStorage.getPriority();
        }
        return 0;
    }

    @Override
    public int getSlot() {
        if (this.gasStorage != null) {
            return this.gasStorage.getSlot();
        }
        return 0;
    }

    @Override
    public boolean validForPass(final int i) {
        return this.gasStorage.validForPass(i);
    }

    @Override
    public IAEGasStack injectItems(final IAEGasStack input, final Actionable type, final IActionSource src) {
        if (this.gasStorage != null) {
            return this.gasStorage.injectItems(input, type, src);
        }
        return input;
    }

    @Override
    public IAEGasStack extractItems(final IAEGasStack request, final Actionable mode, final IActionSource src) {
        if (this.gasStorage != null) {
            return this.gasStorage.extractItems(request, mode, src);
        }
        return null;
    }

    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        if (this.gasStorage != null) {
            return this.gasStorage.getChannel();
        }
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public double extractAEPower(final double amt, @Nonnull final Actionable mode, @Nonnull final PowerMultiplier usePowerMultiplier) {
        if (this.wth != null && this.effectiveItem != null) {
            if (mode == Actionable.SIMULATE) {
                return this.wth.hasPower(this.myPlayer, amt, this.effectiveItem) ? amt : 0;
            }
            return this.wth.usePower(this.myPlayer, amt, this.effectiveItem) ? amt : 0;
        }
        return 0.0;
    }

    @Override
    public ItemStack getItemStack() {
        return this.effectiveItem;
    }

    @Override
    public IConfigManager getConfigManager() {
        return this.wth.getConfigManager(this.effectiveItem);
    }

    @Nonnull
    @Override
    public IGridNode getActionableNode() {
        this.rangeCheck();
        if (this.myWap != null) {
            return this.myWap.getActionableNode();
        } else if (this.myQC != null && this.myQC.getCenter().isPowered()) {
            return this.myQC.getCenter().getActionableNode();
        }
        return null;
    }

    public boolean rangeCheck() {
        this.sqRange = this.myRange = Double.MAX_VALUE;

        if (this.targetGrid != null && this.gasStorage != null) {
            if (this.myWap != null) {
                if (this.myWap.getGrid() == this.targetGrid) {
                    return this.testWap(this.myWap);
                }
                return false;
            }

            IMachineSet tw = this.targetGrid.getMachines(TileWireless.class);

            this.myWap = null;
            this.myQC = null;

            for (final IGridNode n : tw) {
                final IWirelessAccessPoint wap = (IWirelessAccessPoint) n.getMachine();
                if (this.testWap(wap)) {
                    this.myWap = wap;
                }
            }

            if (myWap != null) return true;

            tw = this.targetGrid.getMachines(TileQuantumBridge.class);
            for (final IGridNode n : tw) {
                TileQuantumBridge tqb = (TileQuantumBridge) n.getMachine();
                if (tqb.getCluster() != null) {
                    TileQuantumBridge center = ((QuantumCluster) tqb.getCluster()).getCenter();
                    if (center != null) {
                        if (center.getInternalInventory().getStackInSlot(1).isItemEqual(AEApi.instance().definitions().materials().cardQuantumLink().maybeStack(1).get())) {
                            myQC = (QuantumCluster) tqb.getCluster();
                            myRange = 1;
                            return true;
                        }
                    }
                }
            }

            return this.myWap != null || this.myQC != null;
        }
        return false;
    }

    private boolean testWap(final IWirelessAccessPoint wap) {
        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        final DimensionalCoord dc = wap.getLocation();

        if (dc.getWorld() == this.myPlayer.world) {
            final double offX = dc.x - this.myPlayer.posX;
            final double offY = dc.y - this.myPlayer.posY;
            final double offZ = dc.z - this.myPlayer.posZ;

            final double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && this.sqRange > r) {
                if (wap.isActive()) {
                    this.sqRange = r;
                    this.myRange = Math.sqrt(r);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getInventorySlot() {
        return this.inventorySlot;
    }

    @Override
    public boolean isBaubleSlot() {
        return isBaubleSlot;
    }

    @Override
    public void saveChanges() {
        NBTTagCompound data = effectiveItem.getTagCompound();
        if (data == null) {
            data = new NBTTagCompound();
        }
        upgrades.writeToNBT(data, "upgrades");
    }

    public void saveChanges(NBTTagCompound data) {
        if (effectiveItem.getTagCompound() != null) {
            effectiveItem.getTagCompound().merge(data);
        } else {
            effectiveItem.setTagCompound(data);
        }
    }

    public void loadFromNBT() {
        NBTTagCompound data = effectiveItem.getTagCompound();
        if (data != null) {
            upgrades.readFromNBT(data);
        }
    }

    @Override
    public void onChangeInventory(IItemHandler inv, int slot, InvOperation mc, ItemStack removedStack, ItemStack newStack) {

    }

    @Override
    public IItemHandler getInventoryByName(String name) {
        if (name.equals("upgrades")) {
            return upgrades;
        }
        return null;
    }

}
