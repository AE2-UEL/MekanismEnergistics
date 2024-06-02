package com.mekeng.github.core;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.TunnelType;
import appeng.api.parts.IPart;
import appeng.api.storage.IStorageChannel;
import appeng.api.util.AEPartLocation;
import appeng.core.sync.GuiBridge;
import appeng.core.sync.GuiWrapper;
import appeng.me.GridAccessException;
import appeng.me.cache.P2PCache;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.Platform;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.container.handler.AEGuiBridge;
import com.mekeng.github.common.me.inventory.IExtendedGasHandler;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Ae2Reflect;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

/**
 * You shouldn't use anything from here.<br>
 * All hookers are only used in ASM injection
 */

@SuppressWarnings("unused")
public final class CoreHooks {

    public static GasStack hooker$wrapGasHandler(IGasHandler handler, EnumFacing side, int amount, boolean doTransfer, GasStack stack) {
        if (handler instanceof IExtendedGasHandler) {
            GasStack draw = stack.copy().withAmount(amount);
            return ((IExtendedGasHandler) handler).drawGas(side, draw, doTransfer);
        }
        return handler.drawGas(side, amount, doTransfer);
    }

    public static GuiBridge hooker$getGuiBridge(IStorageChannel<?> channel) {
        if (channel == AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class)) {
            return GuiWrapper.INSTANCE.wrap(AEGuiBridge.GAS_TERMINAL);
        }
        return null;
    }

    public static boolean hooker$customTunnel(PartP2PTunnel<?> p2p, final EntityPlayer player, final EnumHand hand, final Vec3d pos) {
        if (Platform.isClient()) {
            return true;
        }

        if (hand == EnumHand.OFF_HAND) {
            return false;
        }

        ItemStack is = player.getHeldItem(hand);
        if (is.isEmpty()) {
            is = player.getHeldItemOffhand();
        }

        try {
            boolean hasPerms = p2p.getProxy().getSecurity().hasPermission(player, SecurityPermissions.BUILD);
            if (!hasPerms) {
                return false;
            }
        } catch (GridAccessException e) {
            return false;
        }

        final TunnelType tt = AEApi.instance().registries().p2pTunnel().getTunnelTypeByItem(is);
        if (tt == MekEng.GAS) {
            ItemStack newType = new ItemStack(ItemAndBlocks.GAS_P2P);
            if (!newType.isEmpty() && !ItemStack.areItemsEqual(newType, p2p.getItemStack())) {
                final boolean oldOutput = p2p.isOutput();
                final short myFreq = p2p.getFrequency();

                try {
                    p2p.getProxy().getP2P().removeTunnel(p2p, p2p.getFrequency());
                } catch (GridAccessException e) {
                    MekEng.log.error(e);
                }

                p2p.getHost().removePart(p2p.getSide(), false);

                final AEPartLocation dir = p2p.getHost().addPart(newType, p2p.getSide(), player, hand);
                final IPart newBus = p2p.getHost().getPart(dir);

                if (newBus instanceof PartP2PTunnel) {
                    final PartP2PTunnel<?> newTunnel = (PartP2PTunnel<?>) newBus;
                    Ae2Reflect.setP2POutput(newTunnel, oldOutput);
                    try {
                        final P2PCache p2pC = newTunnel.getProxy().getP2P();
                        p2pC.updateFreq(newTunnel, myFreq);
                    } catch (final GridAccessException e) {
                        // :P
                    }
                    newTunnel.onTunnelNetworkChange();
                }
                return true;
            }
        }
        return false;
    }

}
