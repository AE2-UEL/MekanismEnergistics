package com.mekeng.github.util.helpers;

import appeng.api.config.SortDir;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.util.Utils;

import java.util.Comparator;

public class GasSorters {

    private static SortDir Direction = SortDir.ASCENDING;

    public static final Comparator<IAEGasStack> CONFIG_BASED_SORT_BY_NAME = (o1, o2) ->
    {
        if (getDirection() == SortDir.ASCENDING) {
            return Utils.getGasDisplayName(o1).compareToIgnoreCase(Utils.getGasDisplayName(o2));
        }
        return Utils.getGasDisplayName(o2).compareToIgnoreCase(Utils.getGasDisplayName(o1));
    };

    public static final Comparator<IAEGasStack> CONFIG_BASED_SORT_BY_MOD = new Comparator<IAEGasStack>() {

        @Override
        public int compare(final IAEGasStack o1, final IAEGasStack o2) {
            if (getDirection() == SortDir.ASCENDING) {
                return this.secondarySort(Utils.getGasModID(o1).compareToIgnoreCase(Utils.getGasModID(o2)), o2, o1);
            }
            return this.secondarySort(Utils.getGasModID(o1).compareToIgnoreCase(Utils.getGasModID(o2)), o1, o2);
        }

        private int secondarySort(final int compareToIgnoreCase, final IAEGasStack o1, final IAEGasStack o2) {
            if (compareToIgnoreCase == 0) {
                return Utils.getGasDisplayName(o2).compareToIgnoreCase(Utils.getGasDisplayName(o1));
            }
            return compareToIgnoreCase;
        }
    };

    public static final Comparator<IAEGasStack> CONFIG_BASED_SORT_BY_SIZE = (o1, o2) ->
    {
        if (getDirection() == SortDir.ASCENDING) {
            return Long.compare(o2.getStackSize(), o1.getStackSize());
        }
        return Long.compare(o1.getStackSize(), o2.getStackSize());
    };

    private static SortDir getDirection() {
        return Direction;
    }

    public static void setDirection(final SortDir direction) {
        Direction = direction;
    }
    
}
