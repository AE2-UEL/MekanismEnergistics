package com.mekeng.github.util.helpers;

import appeng.api.config.IncludeExclude;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.localization.GuiText;
import com.mekeng.github.common.ItemAndBlocks;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.data.impl.AEGasStack;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.input.Keyboard;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

public class GasCellInfo {

    private static final String[] NUMBER_FORMATS = new String[]{"#.000", "#.00", "#.0", "#"};

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends IAEStack<T>> void addCellInformation(ICellInventoryHandler<T> handler, List<String> lines) {
        if (handler == null) {
            return;
        }

        final ICellInventory<?> cellInventory = handler.getCellInv();

        if (cellInventory != null) {
            lines.add(cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal());
            lines.add(cellInventory.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalItemTypes() + ' ' + GuiText.Types
                    .getLocal());
        }

        if (cellInventory == null) {
            return;
        }

        IItemList<?> itemList = cellInventory.getChannel().createList();

        if (handler.isPreformatted()) {
            final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).getLocal();

            if (handler.isFuzzy()) {
                lines.add("[" + GuiText.Partitioned.getLocal() + "]" + " - " + list + ' ' + GuiText.Fuzzy.getLocal());
            } else {
                lines.add("[" + GuiText.Partitioned.getLocal() + "]" + " - " + list + ' ' + GuiText.Precise.getLocal());
            }

            if (handler.isSticky()) {
                lines.add(GuiText.Sticky.getLocal());
            }

            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                IItemHandler inv = cellInventory.getConfigInventory();
                cellInventory.getAvailableItems((IItemList) itemList);
                for (int i = 0; i < inv.getSlots(); i++) {
                    final ItemStack is = inv.getStackInSlot(i);
                    if (!is.isEmpty()) {
                        if (cellInventory.getChannel() instanceof IGasInventory) {
                            final AEGasStack ais;
                            if (is.getItem() == ItemAndBlocks.DUMMY_GAS) {
                                ais = AEGasStack.of(ItemAndBlocks.DUMMY_GAS.getGasStack(is));
                            } else {
                                ais = AEGasStack.of(Utils.getGasFromItem(is));
                            }
                            IAEGasStack stocked = ((IItemList<IAEGasStack>) itemList).findPrecise(ais);
                            lines.add("[" + is.getDisplayName() + "]" + ": " + (stocked == null ? "0" : gasStackSize(stocked.getStackSize())));
                        }
                    }
                }
            }
        } else {
            if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                cellInventory.getAvailableItems((IItemList) itemList);
                for (IAEStack<?> s : itemList) {
                    if (s instanceof IAEGasStack) {
                        lines.add(((IAEGasStack) s).getGasStack().getGas().getLocalizedName() + ": " + gasStackSize(s.getStackSize()));
                    }
                }
            }
        }
    }

    public static String gasStackSize(long size) {
        String unit;
        if (size >= 1000) {
            unit = "B";
        } else {
            unit = "mB";
        }

        final int log = (int) Math.floor(Math.log10(size)) / 2;

        final int index = Math.max(0, Math.min(3, log));

        final DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        final DecimalFormat format = new DecimalFormat(NUMBER_FORMATS[index]);
        format.setDecimalFormatSymbols(symbols);
        format.setRoundingMode(RoundingMode.DOWN);

        String formatted = format.format(size / 1000d);

        return formatted.concat(unit);
    }

}
