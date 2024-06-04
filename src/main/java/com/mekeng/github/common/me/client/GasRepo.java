package com.mekeng.github.common.me.client;

import appeng.api.AEApi;
import appeng.api.config.Settings;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.config.YesNo;
import appeng.api.storage.data.IItemList;
import appeng.client.gui.widgets.IScrollSource;
import appeng.client.gui.widgets.ISortSource;
import appeng.core.AEConfig;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.GasSorters;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class GasRepo {
    private final IItemList<IAEGasStack> list = AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class).createList();
    private final ArrayList<IAEGasStack> view = new ArrayList<>();
    private final IScrollSource src;
    private final ISortSource sortSrc;

    private int rowSize = 9;

    private String searchString = "";
    private boolean hasPower;

    public GasRepo(final IScrollSource src, final ISortSource sortSrc) {
        this.src = src;
        this.sortSrc = sortSrc;
    }

    public void updateView() {
        this.view.clear();

        this.view.ensureCapacity(this.list.size());

        String innerSearch = this.searchString;

        boolean searchMod = false;
        if (innerSearch.startsWith("@")) {
            searchMod = true;
            innerSearch = innerSearch.substring(1);
        }

        Pattern m;
        try {
            m = Pattern.compile(innerSearch.toLowerCase(), Pattern.CASE_INSENSITIVE);
        } catch (final Exception ignore1) {
            try {
                m = Pattern.compile(Pattern.quote(innerSearch.toLowerCase()), Pattern.CASE_INSENSITIVE);
            } catch (final Exception ignore2) {
                return;
            }
        }

        final Enum<?> viewMode = this.sortSrc.getSortDisplay();
        final boolean needsZeroCopy = viewMode == ViewItems.CRAFTABLE;
        final boolean terminalSearchToolTips = AEConfig.instance().getConfigManager().getSetting(Settings.SEARCH_TOOLTIPS) != YesNo.NO;

        boolean notDone;
        for (IAEGasStack gs : this.list) {

            if (viewMode == ViewItems.CRAFTABLE && !gs.isCraftable()) {
                continue;
            }

            if (viewMode == ViewItems.STORED && gs.getStackSize() == 0) {
                continue;
            }

            final String dspName = searchMod ? Utils.getGasModID(gs) : Utils.getGasDisplayName(gs);
            boolean foundMatchingGasStack = false;
            notDone = true;

            if (m.matcher(dspName.toLowerCase()).find()) {
                notDone = false;
                foundMatchingGasStack = true;
            }

            if (terminalSearchToolTips && notDone && !searchMod) {
                final List<String> tooltip = Platform.getTooltip(gs);

                for (final String line : tooltip) {
                    if (m.matcher(line).find()) {
                        foundMatchingGasStack = true;
                        break;
                    }
                }
            }

            if (foundMatchingGasStack) {
                if (needsZeroCopy) {
                    gs = gs.copy();
                    gs.setStackSize(0);
                }

                this.view.add(gs);
            }
        }

        final Enum<?> sortBy = this.sortSrc.getSortBy();
        final Enum<?> sortDir = this.sortSrc.getSortDir();

        GasSorters.setDirection((appeng.api.config.SortDir) sortDir);

        if (sortBy == SortOrder.MOD) {
            this.view.sort(GasSorters.CONFIG_BASED_SORT_BY_MOD);
        } else if (sortBy == SortOrder.AMOUNT) {
            this.view.sort(GasSorters.CONFIG_BASED_SORT_BY_SIZE);
        } else {
            this.view.sort(GasSorters.CONFIG_BASED_SORT_BY_NAME);
        }
    }

    public void postUpdate(final IAEGasStack is) {
        final IAEGasStack st = this.list.findPrecise(is);
        if (st != null) {
            st.reset();
            st.add(is);
        } else {
            this.list.add(is);
        }
    }

    public IAEGasStack getReferenceGas(int idx) {
        idx += this.src.getCurrentScroll() * this.rowSize;
        if (idx >= this.view.size()) {
            return null;
        }
        return this.view.get(idx);
    }

    public int size() {
        return this.view.size();
    }

    public void clear() {
        this.list.resetStatus();
    }

    public boolean hasPower() {
        return this.hasPower;
    }

    public void setPower(final boolean hasPower) {
        this.hasPower = hasPower;
    }

    public int getRowSize() {
        return this.rowSize;
    }

    public void setRowSize(final int rowSize) {
        this.rowSize = rowSize;
    }

    public String getSearchString() {
        return this.searchString;
    }

    public void setSearchString(@Nonnull final String searchString) {
        this.searchString = searchString;
    }
}
