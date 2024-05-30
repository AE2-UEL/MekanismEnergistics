package com.mekeng.github.common.me.storage.impl;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IItemList;
import com.mekeng.github.common.me.data.IAEGasStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class GasList implements IItemList<IAEGasStack> {

    private final Map<IAEGasStack, IAEGasStack> records = new Object2ObjectOpenHashMap<>();

    protected GasList() {
        // NO-OP
    }

    public static GasList create() {
        return new GasList();
    }

    @Override
    public void addStorage(IAEGasStack option) {
        if (option != null) {
            IAEGasStack st = this.getGasRecord(option);
            if (st != null) {
                st.incStackSize(option.getStackSize());
            } else {
                IAEGasStack opt = option.copy();
                this.putGasRecord(opt);
            }
        }
    }

    @Override
    public void addCrafting(IAEGasStack option) {
        if (option != null) {
            IAEGasStack st = this.getGasRecord(option);
            if (st != null) {
                st.setCraftable(true);
            } else {
                IAEGasStack opt = option.copy();
                opt.setStackSize(0L);
                opt.setCraftable(true);
                this.putGasRecord(opt);
            }
        }
    }

    @Override
    public void addRequestable(IAEGasStack option) {
        if (option != null) {
            IAEGasStack st = this.getGasRecord(option);
            if (st != null) {
                st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
            } else {
                IAEGasStack opt = option.copy();
                opt.setStackSize(0L);
                opt.setCraftable(false);
                opt.setCountRequestable(option.getCountRequestable());
                this.putGasRecord(opt);
            }
        }
    }

    @Override
    public IAEGasStack getFirstItem() {
        for (IAEGasStack gas : this) {
            return gas;
        }
        return null;
    }

    @Override
    public int size() {
        return this.records.values().size();
    }

    @Nonnull
    @Override
    public Iterator<IAEGasStack> iterator() {
        return new MeaningfulGasIterator<>(this.records.values().iterator());
    }

    @Override
    public void resetStatus() {
        for (IAEGasStack gas : this) {
            gas.reset();
        }
    }

    @Override
    public void add(IAEGasStack option) {
        if (option != null) {
            IAEGasStack st = this.getGasRecord(option);
            if (st != null) {
                st.add(option);
            } else {
                IAEGasStack opt = option.copy();
                this.putGasRecord(opt);
            }
        }
    }

    @Override
    public IAEGasStack findPrecise(IAEGasStack gasStack) {
        return gasStack == null ? null : this.getGasRecord(gasStack);
    }

    @Override
    public Collection<IAEGasStack> findFuzzy(IAEGasStack filter, FuzzyMode fuzzyMode) {
        return filter == null ? Collections.emptyList() : Collections.singletonList(this.findPrecise(filter));
    }

    @Override
    public boolean isEmpty() {
        return !this.iterator().hasNext();
    }

    private IAEGasStack getGasRecord(IAEGasStack gas) {
        return this.records.get(gas);
    }

    private void putGasRecord(IAEGasStack gas) {
        this.records.put(gas, gas);
    }

}
