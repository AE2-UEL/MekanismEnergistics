package com.mekeng.github.common.me;

public enum GasTickRates {

    GasStorageBus(5, 60),
    GasImportBus(5, 40),
    GasExportBus(5, 60);

    private final int min;
    private final int max;

    GasTickRates(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    public int getMax() {
        return this.max;
    }

    public int getMin() {
        return this.min;
    }

}
