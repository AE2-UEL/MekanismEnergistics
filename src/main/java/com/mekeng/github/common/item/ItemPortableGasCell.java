package com.mekeng.github.common.item;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.items.contents.CellUpgrades;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import com.mekeng.github.common.container.handler.MkEGuis;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.GasCellConfig;
import com.mekeng.github.util.helpers.GasCellInfo;
import com.mekeng.github.util.helpers.PortableGasCellViewer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ItemPortableGasCell extends AEBasePoweredItem implements IStorageCell<IAEGasStack>, IGuiItem, IItemGroup {
    public ItemPortableGasCell() {
        super(AEConfig.instance().getPortableCellBattery());
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World w, @Nonnull final EntityPlayer player, @Nonnull final EnumHand hand) {
        Utils.openItemGui(player, MkEGuis.PORTABLE_GAS_CELL);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isFull3D() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack stack, final World world, final List<String> lines, final ITooltipFlag advancedTooltips) {
        super.addCheckedInformation(stack, world, lines, advancedTooltips);

        final ICellInventoryHandler<IAEGasStack> cdi = AEApi.instance()
                .registries()
                .cell()
                .getCellInventory(stack, null, AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class));

        GasCellInfo.addCellInformation(cdi, lines);
    }

    @Override
    public int getBytes(@Nonnull final ItemStack cellItem) {
        return 512;
    }

    @Override
    public int getBytesPerType(@Nonnull final ItemStack cellItem) {
        return 8;
    }

    @Override
    public int getTotalTypes(@Nonnull final ItemStack cellItem) {
        return 5;
    }

    @Override
    public boolean isBlackListed(@Nonnull final ItemStack cellItem, @Nonnull final IAEGasStack requestedAddition) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(@Nonnull final ItemStack i) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Nonnull
    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public String getUnlocalizedGroupName(final Set<ItemStack> others, final ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

    @Override
    public boolean isEditable(final ItemStack is) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(final ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(final ItemStack is) {
        return new GasCellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(final ItemStack is, final FuzzyMode fzMode) {
        // NO-OP
    }

    @Override
    public IGuiItemObject getGuiObject(final ItemStack is, final World w, final BlockPos pos) {
        return new PortableGasCellViewer(is, pos.getX());
    }

    @Override
    public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
