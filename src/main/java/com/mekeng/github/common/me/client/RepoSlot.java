package com.mekeng.github.common.me.client;

import com.mekeng.github.common.me.data.IAEGasStack;

public class RepoSlot {

    private final int offset;
    private final int xPos;
    private final int yPos;
    private final GasRepo repo;

    public RepoSlot(GasRepo def, int offset, int displayX, int displayY) {
        this.repo = def;
        this.offset = offset;
        this.xPos = displayX;
        this.yPos = displayY;
    }

    public IAEGasStack getAEStack() {
        return this.repo.getReferenceGas(this.offset);
    }

    public boolean hasPower() {
        return this.repo.hasPower();
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

}
