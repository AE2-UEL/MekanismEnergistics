package com.mekeng.github.common.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IConfigManager;
import appeng.core.AppEng;
import appeng.items.parts.PartModels;
import appeng.me.GridAccessException;
import appeng.me.cache.NetworkMonitor;
import appeng.parts.PartModel;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.mekeng.github.common.container.handler.GuiHandler;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IConfigurableGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.IGasInventoryHost;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Random;

public class PartGasLevelEmitter extends PartGasUpgradeable implements IStackWatcherHost, IConfigManagerHost, IGasInventoryHost, IMEMonitorHandlerReceiver<IAEGasStack>, IConfigurableGasInventory {
    @PartModels
    public static final ResourceLocation MODEL_BASE_OFF = new ResourceLocation(AppEng.MOD_ID, "part/level_emitter_base_off");
    @PartModels
    public static final ResourceLocation MODEL_BASE_ON = new ResourceLocation(AppEng.MOD_ID, "part/level_emitter_base_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_OFF = new ResourceLocation(AppEng.MOD_ID, "part/level_emitter_status_off");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_ON = new ResourceLocation(AppEng.MOD_ID, "part/level_emitter_status_on");
    @PartModels
    public static final ResourceLocation MODEL_STATUS_HAS_CHANNEL = new ResourceLocation(AppEng.MOD_ID, "part/level_emitter_status_has_channel");

    public static final PartModel MODEL_OFF_OFF = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_OFF);
    public static final PartModel MODEL_OFF_ON = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_ON);
    public static final PartModel MODEL_OFF_HAS_CHANNEL = new PartModel(MODEL_BASE_OFF, MODEL_STATUS_HAS_CHANNEL);
    public static final PartModel MODEL_ON_OFF = new PartModel(MODEL_BASE_ON, MODEL_STATUS_OFF);
    public static final PartModel MODEL_ON_ON = new PartModel(MODEL_BASE_ON, MODEL_STATUS_ON);
    public static final PartModel MODEL_ON_HAS_CHANNEL = new PartModel(MODEL_BASE_ON, MODEL_STATUS_HAS_CHANNEL);

    private static final int FLAG_ON = 4;

    private boolean prevState = false;
    private long lastReportedValue = 0;
    private long reportingValue = 0;
    private IStackWatcher stackWatcher = null;
    private final GasInventory config = new GasInventory(1, this);

    public PartGasLevelEmitter(ItemStack is) {
        super(is);
        this.getConfigManager().registerSetting(Settings.REDSTONE_EMITTER, RedstoneMode.HIGH_SIGNAL);
    }

    public long getReportingValue() {
        return this.reportingValue;
    }

    public void setReportingValue(final long v) {
        this.reportingValue = v;
        this.updateState();
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        this.configureWatchers();
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        this.stackWatcher = newWatcher;
        this.configureWatchers();
    }

    @Override
    public void onStackChange(IItemList<?> o, IAEStack<?> fullStack, IAEStack<?> diffStack, IActionSource src, IStorageChannel<?> chan) {
        if (chan == AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class) && fullStack.equals(this.config.getGasStack(0))) {
            this.lastReportedValue = fullStack.getStackSize();
            this.updateState();
        }
    }

    @Override
    public void onGasInventoryChanged(IGasInventory inv, int slot) {
        this.configureWatchers();
    }

    @Override
    @MENetworkEventSubscribe
    public void powerRender(final MENetworkPowerStatusChange powerEvent) {
        if (this.getProxy().isActive()) {
            onListUpdate();
        }
        this.updateState();
    }

    @Override
    @MENetworkEventSubscribe
    public void chanRender(final MENetworkChannelsChanged c) {
        if (this.getProxy().isActive()) {
            onListUpdate();
        }
        this.updateState();
    }

    @Override
    public int isProvidingStrongPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return this.prevState ? 15 : 0;
    }

    @Override
    protected int populateFlags(final int cf) {
        return cf | (this.prevState ? FLAG_ON : 0);
    }

    @Override
    public boolean isValid(final Object effectiveGrid) {
        try {
            return this.getProxy().getGrid() == effectiveGrid;
        } catch (final GridAccessException e) {
            return false;
        }
    }

    @Override
    public void postChange(final IBaseMonitor<IAEGasStack> monitor, final Iterable<IAEGasStack> change, final IActionSource actionSource) {
        this.updateReportingValue((IMEMonitor<IAEGasStack>) monitor);
    }

    @Override
    public void onListUpdate() {
        try {
            final IStorageChannel<IAEGasStack> channel = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
            final IMEMonitor<IAEGasStack> inventory = this.getProxy().getStorage().getInventory(channel);

            this.updateReportingValue(inventory);
        } catch (final GridAccessException e) {
            // ;P
        }
    }

    private void updateState() {
        final boolean isOn = this.isLevelEmitterOn();
        if (this.prevState != isOn) {
            this.getHost().markForUpdate();
            final TileEntity te = this.getHost().getTile();
            this.prevState = isOn;
            Platform.notifyBlocksOfNeighbors(te.getWorld(), te.getPos());
            Platform.notifyBlocksOfNeighbors(te.getWorld(), te.getPos().offset(this.getSide().getFacing()));
        }
    }

    private void configureWatchers() {
        final IGasStorageChannel channel = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);

        if (this.stackWatcher != null) {
            this.stackWatcher.reset();

            final IAEGasStack myStack = AEGasStack.of(this.config.getGasStack(0));

            try {
                if (myStack != null) {
                    this.getProxy().getStorage().getInventory(channel).removeListener(this);
                    this.stackWatcher.add(myStack);
                } else {
                    this.getProxy()
                            .getStorage()
                            .getInventory(channel)
                            .addListener(this, this.getProxy().getGrid());
                }

                final IMEMonitor<IAEGasStack> inventory = this.getProxy().getStorage().getInventory(channel);

                this.updateReportingValue(inventory);
            } catch (GridAccessException e) {
                // NOP
            }
        }
    }

    private void updateReportingValue(final IMEMonitor<IAEGasStack> monitor) {
        final IAEGasStack myStack = AEGasStack.of(this.config.getGasStack(0));

        if (myStack == null) {
            if (monitor instanceof NetworkMonitor) {
                this.lastReportedValue = ((NetworkMonitor<IAEGasStack>) monitor).getGridCurrentCount();
            }
        } else {
            final IAEGasStack r = monitor.getStorageList().findPrecise(myStack);
            if (r == null) {
                this.lastReportedValue = 0;
            } else {
                this.lastReportedValue = r.getStackSize();
            }
        }
        this.updateState();
    }

    private boolean isLevelEmitterOn() {
        if (Platform.isClient()) {
            return (this.getClientFlags() & FLAG_ON) == FLAG_ON;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        final boolean flipState = this.getConfigManager().getSetting(Settings.REDSTONE_EMITTER) == RedstoneMode.LOW_SIGNAL;
        return flipState == (this.reportingValue > this.lastReportedValue);
    }

    @Nonnull
    @Override
    public AECableType getCableConnectionType(final AEPartLocation dir) {
        return AECableType.SMART;
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 16;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    public void randomDisplayTick(final World world, final BlockPos pos, final Random r) {
        if (this.isLevelEmitterOn()) {
            final AEPartLocation d = this.getSide();

            final double d0 = d.xOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d1 = d.yOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
            final double d2 = d.zOffset * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;

            world.spawnParticle(EnumParticleTypes.REDSTONE, 0.5 + pos.getX() + d0, 0.5 + pos.getY() + d1, 0.5 + pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isServer()) {
            GuiHandler.openPartGui(player, this.getTile().getWorld(), this.getTile().getPos(), this.getSide().getFacing(), MkEGuis.GAS_LEVEL_EMITTER);
        }
        return true;
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_HAS_CHANNEL : MODEL_OFF_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return this.isLevelEmitterOn() ? MODEL_ON_ON : MODEL_OFF_ON;
        } else {
            return this.isLevelEmitterOn() ? MODEL_ON_OFF : MODEL_OFF_OFF;
        }
    }

    public IGasInventory getConfig() {
        return this.config;
    }

    @Override
    public IGasInventory getGasInventoryByName(final String name) {
        if (name.equals("config")) {
            return this.config;
        }
        return null;
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);
        this.lastReportedValue = data.getLong("lastReportedValue");
        this.reportingValue = data.getLong("reportingValue");
        this.prevState = data.getBoolean("prevState");
        this.config.load(data.getCompoundTag("config"));
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);
        data.setLong("lastReportedValue", this.lastReportedValue);
        data.setLong("reportingValue", this.reportingValue);
        data.setBoolean("prevState", this.prevState);
        data.setTag("config", this.config.save());
    }

}
