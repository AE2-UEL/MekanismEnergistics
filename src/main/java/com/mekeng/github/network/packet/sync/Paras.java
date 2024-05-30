package com.mekeng.github.network.packet.sync;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public final class Paras {
    private final @Nonnull Object[] paras;

    public Paras(Object[] objs) {
        this.paras = objs == null ? new Object[0] : objs;
    }

    public <T> T get(int index) {
        return (T) this.paras[index];
    }

    public <T> @Nullable T getSoft(int index) {
        return index >= 0 && index < this.paras.length ? (T) this.paras[index] : null;
    }

    public int getParaAmount() {
        return this.paras.length;
    }
}