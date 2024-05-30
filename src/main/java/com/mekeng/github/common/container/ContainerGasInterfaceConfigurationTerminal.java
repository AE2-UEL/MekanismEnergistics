package com.mekeng.github.common.container;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.helpers.InventoryAction;
import appeng.util.Platform;
import com.mekeng.github.MekEng;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.me.inventory.impl.GasInventory;
import com.mekeng.github.common.part.PartGasInterface;
import com.mekeng.github.common.part.PartGasInterfaceConfigurationTerminal;
import com.mekeng.github.common.tile.TileGasInterface;
import com.mekeng.github.network.packet.SGenericPacket;
import com.mekeng.github.network.packet.sync.IActionHolder;
import com.mekeng.github.network.packet.sync.Paras;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.ItemGasHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ContainerGasInterfaceConfigurationTerminal extends AEBaseContainer implements IActionHolder {

    private static long autoBase = Long.MIN_VALUE;
    private final Map<IGasInterfaceHost, GasConfigTracker> diList = new HashMap<>();
    private final Map<Long, GasConfigTracker> byId = new HashMap<>();
    private IGrid grid;
    private NBTTagCompound data = new NBTTagCompound();
    private final Map<String, Consumer<Paras>> holder = createHolder();

    public ContainerGasInterfaceConfigurationTerminal(final InventoryPlayer ip, final PartGasInterfaceConfigurationTerminal anchor) {
        super(ip, anchor);
        if (Platform.isServer()) {
            this.grid = anchor.getActionableNode().getGrid();
        }
        this.bindPlayerInventory(ip, 14, 235 - /* height of player inventory */82);
        this.holder.put("jei_set", o -> this.setFromJei(o.get(0), o.get(1), o.get(2)));
    }

    @Override
    public void detectAndSendChanges() {
        if (Platform.isClient()) {
            return;
        }
        super.detectAndSendChanges();
        if (this.grid == null) {
            return;
        }
        int total = 0;
        boolean missing = false;
        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileGasInterface.class)) {
                    if (gn.isActive()) {
                        final IGasInterfaceHost ih = (IGasInterfaceHost) gn.getMachine();
                        if (ih.getDualityGasInterface().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final GasConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityGasInterface dual = ih.getDualityGasInterface();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartGasInterface.class)) {
                    if (gn.isActive()) {
                        final IGasInterfaceHost ih = (IGasInterfaceHost) gn.getMachine();
                        if (ih.getDualityGasInterface().getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.NO) {
                            continue;
                        }

                        final GasConfigTracker t = this.diList.get(ih);

                        if (t == null) {
                            missing = true;
                        } else {
                            final DualityGasInterface dual = ih.getDualityGasInterface();
                            if (!t.unlocalizedName.equals(dual.getTermName())) {
                                missing = true;
                            }
                        }

                        total++;
                    }
                }
            }
        }

        if (total != this.diList.size() || missing) {
            this.regenList(this.data);
        } else {
            for (final Map.Entry<IGasInterfaceHost, GasConfigTracker> en : this.diList.entrySet()) {
                final GasConfigTracker inv = en.getValue();
                for (int x = 0; x < inv.server.size(); x++) {
                    if ((inv.server.getGasStack(x) == null && inv.client.getGasStack(x) != null) || (inv.server.getGasStack(x) != null && !inv.server.getGasStack(x).equals(inv.client.getGasStack(x)))) {
                        this.addGases(this.data, inv, x, 1);
                    }
                }
            }
        }

        if (!this.data.isEmpty()) {
            MekEng.proxy.netHandler.sendTo(new SGenericPacket("update", this.data), (EntityPlayerMP) this.getPlayerInv().player);
            this.data = new NBTTagCompound();
        }
    }

    @Override
    public void doAction(final EntityPlayerMP player, final InventoryAction action, final int slot, final long id) {
        final GasConfigTracker inv = this.byId.get(id);
        if (inv != null) {
            ItemStack itemInHand = player.inventory.getItemStack();
            ItemGasHandler c = Utils.getItemGasHandler(itemInHand);
            if (c != null) {
                GasStack gs = c.removeGas(Integer.MAX_VALUE, false);
                if (gs != null) {
                    inv.server.setGas(slot, gs);
                    return;
                }
                return;
            }
            inv.server.setGas(slot, null);
            this.updateHeld(player);
        }
    }

    private void setFromJei(int slot, long id, IAEGasStack gas) {
        final GasConfigTracker inv = this.byId.get(id);
        if (inv != null && gas != null) {
            inv.server.setGas(slot, gas.getGasStack());
        }
    }

    private void regenList(final NBTTagCompound data) {
        this.byId.clear();
        this.diList.clear();

        final IActionHost host = this.getActionHost();
        if (host != null) {
            final IGridNode agn = host.getActionableNode();
            if (agn != null && agn.isActive()) {
                for (final IGridNode gn : this.grid.getMachines(TileGasInterface.class)) {
                    final IGasInterfaceHost ih = (IGasInterfaceHost) gn.getMachine();
                    final DualityGasInterface dual = ih.getDualityGasInterface();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new GasConfigTracker(dual, dual.getConfig(), dual.getTermName()));
                    }
                }

                for (final IGridNode gn : this.grid.getMachines(PartGasInterface.class)) {
                    final IGasInterfaceHost ih = (IGasInterfaceHost) gn.getMachine();
                    final DualityGasInterface dual = ih.getDualityGasInterface();
                    if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                        this.diList.put(ih, new GasConfigTracker(dual, dual.getConfig(), dual.getTermName()));
                    }
                }
            }
        }

        data.setBoolean("clear", true);

        for (final Map.Entry<IGasInterfaceHost, GasConfigTracker> en : this.diList.entrySet()) {
            final GasConfigTracker inv = en.getValue();
            this.byId.put(inv.which, inv);
            this.addGases(data, inv, 0, inv.server.size());
        }
    }

    private void addGases(final NBTTagCompound data, final GasConfigTracker inv, final int offset, final int length) {
        final String name = '=' + Long.toString(inv.which, Character.MAX_RADIX);
        final NBTTagCompound tag = data.getCompoundTag(name);

        if (tag.isEmpty()) {
            tag.setLong("sortBy", inv.sortBy);
            tag.setString("un", inv.unlocalizedName);
            tag.setTag("pos", NBTUtil.createPosTag(inv.pos));
            tag.setInteger("dim", inv.dim);
        }

        for (int x = 0; x < length; x++) {
            NBTTagCompound gasNBT = new NBTTagCompound();
            GasStack gasStack = inv.server.getGasStack(x + offset);

            // "update" client side.
            inv.client.setGas(x + offset, gasStack == null ? null : gasStack.copy());

            if (gasStack != null) {
                gasNBT = gasStack.write(gasNBT);
            }

            tag.setTag(Integer.toString(x + offset), gasNBT);
        }

        data.setTag(name, tag);
    }

    @Nonnull
    @Override
    public Map<String, Consumer<Paras>> getActionMap() {
        return this.holder;
    }

    public static class GasConfigTracker {

        private final long sortBy;
        private final long which = autoBase++;
        private final String unlocalizedName;
        private final IGasInventory client;
        private final IGasInventory server;
        private final BlockPos pos;
        private final int dim;

        public GasConfigTracker(final DualityGasInterface dual, final IGasInventory configSlots, final String unlocalizedName) {
            this.server = configSlots;
            this.client = new GasInventory(this.server.size());
            this.unlocalizedName = unlocalizedName;
            this.sortBy = dual.getSortValue();
            this.pos = dual.getLocation().getPos();
            this.dim = dual.getLocation().getWorld().provider.getDimension();
        }

    }
}
