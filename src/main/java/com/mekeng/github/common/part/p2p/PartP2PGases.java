package com.mekeng.github.common.part.p2p;

import appeng.api.parts.IPartModel;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnel;
import com.google.common.collect.Maps;
import com.mekeng.github.MekEng;
import com.mekeng.github.util.Ae2Reflect;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.api.gas.GasTankInfo;
import mekanism.api.gas.IGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PartP2PGases extends PartP2PTunnel<PartP2PGases> implements IGasHandler {

    private static final Object MODELS = Ae2Reflect.createP2PModel("part/p2p/p2p_tunnel_gases");

    private static final ThreadLocal<Deque<PartP2PGases>> DEPTH = new ThreadLocal<>();
    private static final GasTankInfo[] ACTIVE_TANK = {new GasTank(100000)};
    private static final GasTankInfo[] INACTIVE_TANK = {new GasTank(0)};

    private Map.Entry<IGasHandler, EnumFacing> cachedGasTank;
    private int tmpUsed;

    public PartP2PGases(final ItemStack is) {
        super(is);
    }

    public static Collection<ResourceLocation> getModels() {
        Set<ResourceLocation> all = new HashSet<>();
        Ae2Reflect.getP2PModel(MODELS).forEach(m -> all.addAll(m.getModels()));
        return all;
    }

    @Override
    public void onTunnelNetworkChange() {
        this.cachedGasTank = null;
    }

    @Override
    public void onNeighborChanged(IBlockAccess w, BlockPos pos, BlockPos neighbor) {
        this.cachedGasTank = null;
        if (this.isOutput()) {
            try {
                for (PartP2PGases in : this.getInputs()) {
                    if (in != null) {
                        in.onTunnelNetworkChange();
                    }
                }
            } catch (GridAccessException e) {
                MekEng.log.error(e);
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capabilityClass) {
        if (capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capabilityClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capabilityClass) {
        if (capabilityClass == Capabilities.GAS_HANDLER_CAPABILITY) {
            return (T) this;
        }
        return super.getCapability(capabilityClass);
    }

    @Nonnull
    @Override
    public IPartModel getStaticModels() {
        return Ae2Reflect.getP2PModel(MODELS, this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public GasTankInfo[] getTankInfo() {
        if (!this.isOutput()) {
            try {
                for (PartP2PGases tun : this.getInputs()) {
                    if (tun != null) {
                        return ACTIVE_TANK;
                    }
                }
            } catch (GridAccessException e) {
                MekEng.log.error(e);
            }
        }
        return INACTIVE_TANK;
    }

    @Override
    public int receiveGas(EnumFacing side, GasStack resource, boolean doTransfer) {
        if (side != null && side != this.getSide().getFacing()) {
            return 0;
        }
        final Deque<PartP2PGases> stack = this.getDepth();

        for (final PartP2PGases t : stack) {
            if (t == this) {
                return 0;
            }
        }

        stack.push(this);

        final List<PartP2PGases> list = this.getGasOutputs();
        int requestTotal = 0;

        Iterator<PartP2PGases> i = list.iterator();

        while (i.hasNext()) {
            final PartP2PGases l = i.next();
            final Map.Entry<IGasHandler, EnumFacing> gasTank = l.getTarget();
            if (gasTank != null) {
                l.tmpUsed = gasTank.getKey().receiveGas(gasTank.getValue(), resource.copy(), false);
            } else {
                l.tmpUsed = 0;
            }

            if (l.tmpUsed <= 0) {
                i.remove();
            } else {
                requestTotal += l.tmpUsed;
            }
        }

        if (requestTotal <= 0) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }
            return 0;
        }

        if (!doTransfer) {
            if (stack.pop() != this) {
                throw new IllegalStateException("Invalid Recursion detected.");
            }
            return Math.min(resource.amount, requestTotal);
        }

        int available = resource.amount;

        i = list.iterator();
        int used = 0;

        while (i.hasNext() && available > 0) {
            final PartP2PGases l = i.next();

            final GasStack insert = resource.copy();
            insert.amount = (int) Math.ceil(insert.amount * ((double) l.tmpUsed / (double) requestTotal));
            if (insert.amount > available) {
                insert.amount = available;
            }

            final Map.Entry<IGasHandler, EnumFacing> gasTank = l.getTarget();
            if (gasTank != null) {
                l.tmpUsed = gasTank.getKey().receiveGas(gasTank.getValue(), insert.copy(), true);
            } else {
                l.tmpUsed = 0;
            }

            available -= insert.amount;
            used += l.tmpUsed;
        }

        if (stack.pop() != this) {
            throw new IllegalStateException("Invalid Recursion detected.");
        }

        return used;
    }

    @Override
    public GasStack drawGas(EnumFacing side, int amount, boolean doTransfer) {
        return null;
    }

    @Override
    public boolean canReceiveGas(EnumFacing side, Gas type) {
        return true;
    }

    @Override
    public boolean canDrawGas(EnumFacing side, Gas type) {
        return false;
    }

    private Deque<PartP2PGases> getDepth() {
        Deque<PartP2PGases> s = DEPTH.get();
        if (s == null) {
            DEPTH.set(s = new ArrayDeque<>());
        }
        return s;
    }

    private List<PartP2PGases> getGasOutputs() {
        final List<PartP2PGases> outs = new ArrayList<>();

        try {
            for (final PartP2PGases l : this.getOutputs()) {
                final Map.Entry<IGasHandler, EnumFacing> handler = l.getTarget();
                if (handler != null) {
                    outs.add(l);
                }
            }
        } catch (final GridAccessException e) {
            // :P
        }

        return outs;
    }

    private Map.Entry<IGasHandler, EnumFacing> getTarget() {
        if (!this.getProxy().isActive()) {
            return null;
        }

        if (this.cachedGasTank != null) {
            return this.cachedGasTank;
        }

        final TileEntity te = this.getTile().getWorld().getTileEntity(this.getTile().getPos().offset(this.getSide().getFacing()));

        if (te != null && te.hasCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite())) {
            return this.cachedGasTank = Maps.immutableEntry(
                    te.getCapability(Capabilities.GAS_HANDLER_CAPABILITY, this.getSide().getFacing().getOpposite()),
                    this.getSide().getFacing().getOpposite()
            );
        }

        return null;
    }

}