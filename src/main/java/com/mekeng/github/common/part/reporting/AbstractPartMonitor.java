package com.mekeng.github.common.part.reporting;

import appeng.api.AEApi;
import appeng.api.implementations.parts.IPartStorageMonitor;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.client.render.TesrRenderHelper;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.Reflected;
import appeng.me.GridAccessException;
import appeng.parts.reporting.AbstractPartDisplay;
import appeng.parts.reporting.PartPanel;
import appeng.util.IWideReadableNumberConverter;
import appeng.util.Platform;
import appeng.util.ReadableNumberConverter;
import com.mekeng.github.client.render.GasRenderHelper;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.ItemGasHandler;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public abstract class AbstractPartMonitor extends AbstractPartDisplay implements IPartStorageMonitor, IStackWatcherHost {
    private static final IWideReadableNumberConverter NUMBER_CONVERTER = ReadableNumberConverter.INSTANCE;

    private IAEGasStack configuredGas;
//    private IAEItemStack configuredItem;
    private long configuredAmount;
    private boolean isLocked;
    private IStackWatcher myWatcher;
    @Reflected
    public AbstractPartMonitor(ItemStack is) {
        super(is);
    }

    @Override
    public void readFromNBT(final NBTTagCompound data) {
        super.readFromNBT(data);

        this.isLocked = data.getBoolean("isLocked");

        final NBTTagCompound myGas = data.getCompoundTag("configuredGas");
        this.configuredGas = AEGasStack.of(myGas);
    }

    @Override
    public void writeToNBT(final NBTTagCompound data) {
        super.writeToNBT(data);

        data.setBoolean("isLocked", this.isLocked);

        final NBTTagCompound myGas = new NBTTagCompound();
        if (this.configuredGas != null) {
            this.configuredGas.writeToNBT(myGas);
        }

        data.setTag("configuredGas", myGas);
    }

    @Override
    public void writeToStream(final ByteBuf data) throws IOException {
        super.writeToStream(data);

        data.writeBoolean(this.isLocked);
        //is configured
        data.writeBoolean(this.configuredGas != null);
        if (this.configuredGas != null) {
            this.configuredGas.writeToPacket(data);
        }
    }

    @Override
    public boolean readFromStream(final ByteBuf data) throws IOException {
        boolean needRedraw = super.readFromStream(data);

        final boolean isLocked = data.readBoolean();
        needRedraw = this.isLocked != isLocked;

        this.isLocked = isLocked;

        final boolean isGas = data.readBoolean();
        if (isGas) {
            this.configuredGas = AEGasStack.of(data);
        } else {
            this.configuredGas = null;
        }

        return needRedraw;
    }

    @Override
    public boolean onPartActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        if (!this.isLocked) {
            GasStack gasInTank = null;
            final ItemStack eq = player.getHeldItem(hand);
            ItemGasHandler itemGasHandler = Utils.getItemGasHandler(eq);
            if (itemGasHandler != null) {
                gasInTank = itemGasHandler.gasStack();
            }


            if (gasInTank == null) {
                this.configuredGas = null;
            } else  {
                this.configuredGas = AEGasStack.of(gasInTank).setStackSize(0);
            }

            this.configureWatchers();
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        } else {
            return super.onPartActivate(player, hand, pos);
        }

        return true;
    }

    @Override
    public boolean onPartShiftActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (!this.getProxy().isActive()) {
            return false;
        }

        if (!Platform.hasPermissions(this.getLocation(), player)) {
            return false;
        }

        if (player.getHeldItem(hand).isEmpty()) {
            this.isLocked = !this.isLocked;
            player.sendMessage((this.isLocked ? PlayerMessages.isNowLocked : PlayerMessages.isNowUnlocked).get());
            this.getHost().markForSave();
            this.getHost().markForUpdate();
        }

        return true;
    }

    private void configureWatchers() {
        if (this.myWatcher != null) {
            this.myWatcher.reset();
        }

        try {
             if (this.configuredGas != null) {
                if (this.myWatcher != null) {
                    this.myWatcher.add(this.configuredGas);
                }

                this.updateReportingValue(
                        this.getProxy().getStorage().getInventory(AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)));
            }
        } catch (final GridAccessException e) {
            // >.>
        }
    }

    private <T extends IAEStack<T>> void updateReportingValue(final IMEMonitor<T> monitor) {
        if (this.configuredGas != null) {
            final IAEGasStack result = (IAEGasStack) monitor.getStorageList().findPrecise((T) this.configuredGas);
            if (result == null) {
                this.configuredAmount = 0;
            } else {
                this.configuredAmount = result.getStackSize();
            }
            this.configuredGas.setStackSize(this.configuredAmount);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderDynamic(double x, double y, double z, float partialTicks, int destroyStage) {

        if ((this.getClientFlags() & (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG)) != (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG)) {
            return;
        }

        IAEStack<?> ais = this.getDisplayed();

        if (ais == null) {
            return;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        EnumFacing facing = this.getSide().getFacing();

        TesrRenderHelper.moveToFace(facing);
        TesrRenderHelper.rotateToFace(facing, this.getSpin());
        if (ais instanceof IAEGasStack){
            GasRenderHelper.renderGas2dWithAmount((IAEGasStack) ais,.8f, 0.17f);
        }
        GlStateManager.popMatrix();

    }

    @Override
    public boolean requireDynamicRender() {
        return true;
    }

    @Override
    public IAEStack<?> getDisplayed() {
        if (this.configuredGas != null)
            return this.configuredGas;
        return null;
    }

    @Override
    public boolean isLocked() {
        return this.isLocked;
    }

    @Override
    public void updateWatcher(final IStackWatcher newWatcher) {
        this.myWatcher = newWatcher;
        this.configureWatchers();
    }

    @MENetworkEventSubscribe
    public void powerStatusChange(final MENetworkPowerStatusChange ev) {
        if (this.getProxy().isPowered()) {
            this.configureWatchers();
        }
    }

    @MENetworkEventSubscribe
    public void channelChanged(final MENetworkChannelsChanged c) {
        if (this.getProxy().isPowered()) {
            this.configureWatchers();
        }
    }

    @Override
    public void onStackChange(IItemList<?> o, IAEStack<?> fullStack, IAEStack<?> diffStack, IActionSource src, IStorageChannel<?> chan) {
        this.configuredAmount = fullStack.getStackSize();

        if (this.configuredGas != null) {
            this.configuredGas.setStackSize(this.configuredAmount);
        }
        this.getHost().markForUpdate();
    }

    @Override
    public boolean showNetworkInfo(final RayTraceResult where) {
        return false;
    }

    protected IPartModel selectModel(IPartModel off, IPartModel on, IPartModel hasChannel, IPartModel lockedOff, IPartModel lockedOn, IPartModel lockedHasChannel) {
        if (this.isActive()) {
            if (this.isLocked()) {
                return lockedHasChannel;
            } else {
                return hasChannel;
            }
        } else if (this.isPowered()) {
            if (this.isLocked()) {
                return lockedOn;
            } else {
                return on;
            }
        } else {
            if (this.isLocked()) {
                return lockedOff;
            } else {
                return off;
            }
        }
    }
}
