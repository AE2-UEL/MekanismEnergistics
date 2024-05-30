package com.mekeng.github.common.part;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.IConfigManager;
import appeng.capabilities.Capabilities;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiWrapper;
import appeng.helpers.IPriorityHost;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.helpers.MachineSource;
import appeng.me.storage.ITickingMonitor;
import appeng.me.storage.MEInventoryHandler;
import appeng.parts.PartModel;
import appeng.tile.networking.TileCableBus;
import appeng.util.ConfigManager;
import appeng.util.Platform;
import appeng.util.prioritylist.FuzzyPriorityList;
import appeng.util.prioritylist.PrecisePriorityList;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.container.handler.AEGuiBridge;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.common.me.GasTickRates;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.inventory.IConfigurableGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import com.mekeng.github.common.me.inventory.impl.GasHandlerAdapter;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import mekanism.api.gas.IGasHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PartGasStorageBus extends PartGasUpgradeable implements IGridTickable, ICellContainer, IMEMonitorHandlerReceiver<IAEGasStack>, IGasInventoryHost, IConfigurableGasInventory, IPriorityHost {
    public static final ResourceLocation MODEL_BASE = new ResourceLocation(MekEng.MODID, "part/gas_storage_bus_base");
    @PartModels
    public static final IPartModel MODELS_OFF = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_storage_bus_off"));
    @PartModels
    public static final IPartModel MODELS_ON = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_storage_bus_on"));
    @PartModels
    public static final IPartModel MODELS_HAS_CHANNEL = new PartModel(MODEL_BASE, new ResourceLocation(MekEng.MODID, "part/gas_storage_bus_has_channel"));

    private final IActionSource source;
    private final GasInventory config = new GasInventory(63, this);
    private int priority = 0;
    private boolean cached = false;
    private ITickingMonitor monitor = null;
    private MEInventoryHandler<IAEGasStack> handler = null;
    private int handlerHash = 0;
    private boolean wasActive = false;
    private byte resetCacheLogic = 0;
    private boolean accessChanged;
    private boolean readOncePass;

    public PartGasStorageBus(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.ACCESS, AccessRestriction.READ_WRITE);
        this.getConfigManager().registerSetting(Settings.FUZZY_MODE, FuzzyMode.IGNORE_ALL);
        this.getConfigManager().registerSetting(Settings.STORAGE_FILTER, StorageFilter.EXTRACTABLE_ONLY);
        this.source = new MachineSource(this);
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange c) {
        this.updateStatus();
    }

    protected void updateStatus() {
        final boolean currentActive = this.getProxy().isActive();
        if (this.wasActive != currentActive) {
            this.wasActive = currentActive;
            try {
                this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
                this.getHost().markForUpdate();
            } catch (final GridAccessException e) {
                // :P
            }
        }
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged changedChannels) {
        this.updateStatus();
    }

    @Override
    protected int getUpgradeSlots() {
        return 5;
    }

    @Override
    public void updateSetting(final IConfigManager manager, final Enum settingName, final Enum newValue) {
        if (settingName.name().equals("ACCESS")) {
            this.accessChanged = true;
        }
        this.resetCache(true);
        this.getHost().markForSave();
    }

    @Override
    public void onGasInventoryChanged(IGasInventory inv, int slot) {
        if (inv == this.config) {
            this.resetCache(true);
        }
    }

    @Override
    public void upgradesChanged() {
        super.upgradesChanged();
        this.resetCache(true);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.config.load(data.getCompoundTag("config"));
        this.priority = data.getInteger("priority");
        this.accessChanged = false;
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("config", this.config.save());
        data.setInteger("priority", this.priority);
    }

    @Override
    public IGasInventory getGasInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    protected void resetCache(final boolean fullReset) {
        if (this.getHost() == null || this.getHost().getTile() == null || this.getHost().getTile().getWorld() == null || this.getHost().getTile().getWorld().isRemote) {
            return;
        }

        if (fullReset) {
            this.resetCacheLogic = 2;
        } else if (this.resetCacheLogic < 2) {
            this.resetCacheLogic = 1;
        }

        try {
            this.getProxy().getTick().alertDevice(this.getProxy().getNode());
        } catch (final GridAccessException e) {
            // :P
        }
    }

    @Override
    public boolean isValid(final Object verificationToken) {
        return this.handler == verificationToken;
    }

    @Override
    public void postChange(final IBaseMonitor<IAEGasStack> monitor, final Iterable<IAEGasStack> change, final IActionSource source) {
        if (this.getProxy().isActive()) {
            Iterable<IAEGasStack> filteredChanges = this.filterChanges(change);

            AccessRestriction currentAccess = (AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS);
            if (readOncePass) {
                readOncePass = false;
                try {
                    this.getProxy().getStorage().postAlterationOfStoredItems(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class), filteredChanges, this.source);
                } catch (final GridAccessException e) {
                    // :(
                }
                return;
            }
            if (!currentAccess.hasPermission(AccessRestriction.READ)) {
                return;
            }
            try {
                this.getProxy().getStorage().postAlterationOfStoredItems(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class), filteredChanges, source);
            } catch (final GridAccessException e) {
                // :(
            }
        }
    }

    @Override
    public void onListUpdate() {
        // not used here.
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(3, 3, 15, 13, 13, 16);
        bch.addBox(2, 2, 14, 14, 14, 15);
        bch.addBox(5, 5, 12, 11, 11, 14);
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        if (pos.offset(this.getSide().getFacing()).equals(neighbor)) {

            final TileEntity te = w.getTileEntity(neighbor);

            // In case the TE was destroyed, we have to do a full reset immediately.
            if (te instanceof TileCableBus) {
                IPart iPart = ((TileCableBus) te).getPart(this.getSide().getOpposite());
                if (iPart == null || iPart instanceof IGasInterfaceHost) {
                    this.resetCache(true);
                    this.resetCache();
                }
            } else if (te == null || te instanceof IGasInterfaceHost) {
                this.resetCache(true);
                this.resetCache();
            } else {
                this.resetCache(false);
            }
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 4;
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer() && this.getActionableNode() != null) {
            GuiHandler.openPartGui(player, this.getTile().getWorld(), this.getTile().getPos(), this.getSide().getFacing(), MkEGuis.GAS_STORAGE_BUS);
        }
        return true;
    }

    @Nonnull
    @Override
    public TickingRequest getTickingRequest(@Nonnull IGridNode node) {
        return new TickingRequest(GasTickRates.GasStorageBus.getMin(), GasTickRates.GasStorageBus.getMax(), monitor == null, true);
    }

    @Nonnull
    @Override
    public TickRateModulation tickingRequest(@Nonnull IGridNode node, int ticksSinceLastCall) {
        if (this.resetCacheLogic != 0) {
            this.resetCache();
        }

        if (this.monitor != null) {
            return this.monitor.onTick();
        }

        return TickRateModulation.SLEEP;
    }

    protected void resetCache() {
        final boolean fullReset = this.resetCacheLogic == 2;
        this.resetCacheLogic = 0;

        final MEInventoryHandler<IAEGasStack> in = this.getInternalHandler();
        IItemList<IAEGasStack> before = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();
        if (in != null) {
            if (accessChanged) {
                AccessRestriction currentAccess = (AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS);
                AccessRestriction oldAccess = (AccessRestriction) ((ConfigManager) this.getConfigManager()).getOldSetting(Settings.ACCESS);
                if (oldAccess.hasPermission(AccessRestriction.READ) && !currentAccess.hasPermission(AccessRestriction.READ)) {
                    readOncePass = true;
                }
                in.setBaseAccess(oldAccess);
                before = in.getAvailableItems(before);
                in.setBaseAccess(currentAccess);
                accessChanged = false;
            } else {
                before = in.getAvailableItems(before);
            }
        }

        this.cached = false;
        if (fullReset) {
            this.handlerHash = 0;
        }

        final MEInventoryHandler<IAEGasStack> out = this.getInternalHandler();
        IItemList<IAEGasStack> after = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();

        if (in != out) {
            if (out != null) {
                after = out.getAvailableItems(after);
            }
            Platform.postListChanges(before, after, this, this.source);
        }
    }

    private IMEInventory<IAEGasStack> getInventoryWrapper(TileEntity target) {
        EnumFacing targetSide = this.getSide().getFacing().getOpposite();
        // Prioritize a handler to directly link to another ME network
        IStorageMonitorableAccessor accessor = target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide);
        if (accessor != null) {
            IStorageMonitorable inventory = accessor.getInventory(this.source);
            if (inventory != null) {
                return inventory.getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));
            }

            // So this could / can be a design decision. If the tile does support our custom capability,
            // but it does not return an inventory for the action source, we do NOT fall back to using
            // IItemHandler's, as that might circumvent the security setings, and might also cause
            // performance issues.
            return null;
        }

        // Check via cap for IItemHandler
        IGasHandler handlerExt = target.getCapability(mekanism.common.capabilities.Capabilities.GAS_HANDLER_CAPABILITY, targetSide);
        if (handlerExt != null) {
            return new GasHandlerAdapter(handlerExt, this, targetSide);
        }
        return null;
    }

    private int createHandlerHash(TileEntity target) {
        if (target == null) {
            return 0;
        }

        final EnumFacing targetSide = this.getSide().getFacing().getOpposite();

        if (target.hasCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide)) {
            return Objects.hash(target, target.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, targetSide));
        }

        final IGasHandler gasHandler = target.getCapability(mekanism.common.capabilities.Capabilities.GAS_HANDLER_CAPABILITY, targetSide);

        if (gasHandler != null) {
            return Objects.hash(target, gasHandler, gasHandler.getTankInfo().length);
        }

        return 0;
    }

    @SuppressWarnings("unchecked")
    public MEInventoryHandler<IAEGasStack> getInternalHandler() {
        if (this.cached) {
            return this.handler;
        }

        final boolean wasSleeping = this.monitor == null;

        this.cached = true;
        final TileEntity self = this.getHost().getTile();
        final TileEntity target = self.getWorld().getTileEntity(self.getPos().offset(this.getSide().getFacing()));
        final int newHandlerHash = this.createHandlerHash(target);

        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash) {
            return this.handler;
        }

        this.handlerHash = newHandlerHash;
        this.handler = null;
        if (this.monitor != null) {
            ((IBaseMonitor<IAEGasStack>) monitor).removeListener(this);
        }
        this.monitor = null;
        if (target != null) {
            IMEInventory<IAEGasStack> inv = this.getInventoryWrapper(target);
            if (inv instanceof ITickingMonitor) {
                this.monitor = (ITickingMonitor) inv;
                this.monitor.setActionSource(this.source);
                this.monitor.setMode((StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER));
            }

            if (inv != null) {
                this.handler = new MEInventoryHandler<>(inv, AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));

                this.handler.setBaseAccess((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS));
                this.handler.setWhitelist(this.getInstalledUpgrades(Upgrades.INVERTER) > 0 ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
                this.handler.setPriority(this.getPriority());
                this.handler.setStorageFilter((StorageFilter) this.getConfigManager().getSetting(Settings.STORAGE_FILTER));

                final IItemList<IAEGasStack> priorityList = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();

                final int slotsToUse = 18 + this.getInstalledUpgrades(Upgrades.CAPACITY) * 9;
                for (int x = 0; x < this.config.size() && x < slotsToUse; x++) {
                    final IAEGasStack is = AEGasStack.of(this.config.getGasStack(x));
                    if (is != null) {
                        priorityList.add(is);
                    }
                }

                if (this.getInstalledUpgrades(Upgrades.STICKY) > 0) {
                    this.handler.setSticky(true);
                }

                if (this.getInstalledUpgrades(Upgrades.FUZZY) > 0) {
                    this.handler.setPartitionList(new FuzzyPriorityList<>(priorityList, (FuzzyMode) this.getConfigManager().getSetting(Settings.FUZZY_MODE)));
                } else {
                    this.handler.setPartitionList(new PrecisePriorityList<>(priorityList));
                }

                if (inv instanceof IBaseMonitor) {
                    if (((AccessRestriction) this.getConfigManager().getSetting(Settings.ACCESS)).hasPermission(AccessRestriction.READ)) {
                        ((IBaseMonitor<IAEGasStack>) inv).addListener(this, this.handler);
                    }
                }
            }
        }

        // update sleep state...
        if (wasSleeping != (this.monitor == null)) {
            try {
                final ITickManager tm = this.getProxy().getTick();
                if (this.monitor == null) {
                    tm.sleepDevice(this.getProxy().getNode());
                } else {
                    tm.wakeDevice(this.getProxy().getNode());
                }
            } catch (final GridAccessException ignore) {
                // :(
            }
        }

        try {
            // force grid to update handlers...
            ((GridStorageCache) this.getProxy().getGrid().getCache(IStorageGrid.class)).cellUpdate(null);
        } catch (final GridAccessException e) {
            // :3
        }

        return this.handler;
    }

    @Override
    public List<IMEInventoryHandler> getCellArray(final IStorageChannel channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)) {
            final IMEInventoryHandler<IAEGasStack> out = this.getInternalHandler();
            if (out != null) {
                return Collections.singletonList(out);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public void setPriority(int newValue) {
        this.priority = newValue;
        this.getHost().markForSave();
        this.resetCache(true);
    }

    @Override
    public void blinkCell(int slot) {

    }

    @Override
    public void saveChanges(@Nullable ICellInventory<?> cellInventory) {

    }

    public IGasInventory getConfig() {
        return this.config;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }

    @Override
    public ItemStack getItemStackRepresentation() {
        return new ItemStack(ItemAndBlocks.GAS_STORAGE_BUS);
    }

    @Override
    public GuiBridge getGuiBridge() {
        return GuiWrapper.INSTANCE.wrap(AEGuiBridge.GAS_STORAGE_BUS);
    }

    // TODO: 1/28/2024 Unify both methods.
    /**
     * Filters the changes to only include items that pass the storage filter.
     * Optimally, this should be handled by the underlying monitor.
     *
     * @see appeng.parts.misc.PartStorageBus#filterChanges
     */
    protected Iterable<IAEGasStack> filterChanges(Iterable<IAEGasStack> change) {
        Enum<?> storageFilter = this.getConfigManager().getSetting(Settings.STORAGE_FILTER);
        if (storageFilter == StorageFilter.EXTRACTABLE_ONLY && this.handler != null) {
            ArrayList<IAEGasStack> filteredList = new ArrayList<>();
            for (final IAEGasStack stack : change) {
                if (this.handler.passesBlackOrWhitelist(stack)) {
                    filteredList.add(stack);
                }
            }
            return filteredList;
        }
        return change;
    }
}
