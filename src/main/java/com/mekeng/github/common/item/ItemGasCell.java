package com.mekeng.github.common.item;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.storage.IGasStorageChannel;
import com.mekeng.github.util.helpers.GasCellConfig;
import com.mekeng.github.util.helpers.GasCellInfo;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

public class ItemGasCell extends AEBaseItem implements IStorageCell<IAEGasStack>, IItemGroup {

    private final ItemStack core;
    private final ItemStack casing;
    private final int totalBytes;
    private final double idleDrain;
    private final int perType;

    public ItemGasCell(ItemStack core, ItemStack casing, int kilobytes, double powerDrain, int typeCost) {
        setMaxStackSize(1);
        this.core = core.copy();
        this.casing = casing.copy();
        this.totalBytes = kilobytes * 1024;
        this.idleDrain = powerDrain;
        this.perType = typeCost;
    }

    public ItemGasCell(Item core, ItemStack casing, int kilobytes, double powerDrain, int typeCost) {
        this(new ItemStack(core), casing, kilobytes, powerDrain, typeCost);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, @Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
        this.disassembleDrive(player.getHeldItem(hand), player);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUseFirst(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EnumHand hand) {
        return this.disassembleDrive(player.getHeldItem(hand), player) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
    }

    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(ItemStack stack, World world, List<String> lines, ITooltipFlag advancedTooltips) {
        GasCellInfo.addCellInformation(AEApi.instance().registries().cell().getCellInventory(stack, null, this.getChannel()), lines);
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack itemStack) {
        return this.casing.copy();
    }

    @Override
    public boolean hasContainerItem(@Nonnull ItemStack stack) {
        return AEConfig.instance().isFeatureEnabled(AEFeature.ENABLE_DISASSEMBLY_CRAFTING);
    }

    @Override
    public int getBytes(@Nonnull ItemStack itemStack) {
        return this.totalBytes;
    }

    @Override
    public int getBytesPerType(@Nonnull ItemStack itemStack) {
        return this.perType;
    }

    @Override
    public int getTotalTypes(@Nonnull ItemStack itemStack) {
        return 15;
    }

    @Override
    public boolean isBlackListed(@Nonnull ItemStack itemStack, @Nonnull IAEGasStack gasStack) {
        return false;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(@Nonnull ItemStack itemStack) {
        return true;
    }

    @Override
    public double getIdleDrain() {
        return this.idleDrain;
    }

    @Nonnull
    @Override
    public IStorageChannel<IAEGasStack> getChannel() {
        return AEApi.instance().storage().getStorageChannel(IGasStorageChannel.class);
    }

    @Override
    public boolean isEditable(ItemStack itemStack) {
        return true;
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack is) {
        return new CellUpgrades(is, 2);
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack is) {
        return new GasCellConfig(is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack itemStack) {
        return FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack itemStack, FuzzyMode fuzzyMode) {

    }

    private boolean disassembleDrive(ItemStack stack, EntityPlayer player) {
        if (player.isSneaking()) {
            if (Platform.isClient()) {
                return false;
            }
            InventoryPlayer playerInventory = player.inventory;
            IMEInventoryHandler<IAEGasStack> inv = AEApi.instance().registries().cell().getCellInventory(stack, null, this.getChannel());
            if (inv != null && playerInventory.getCurrentItem() == stack) {
                InventoryAdaptor ia = InventoryAdaptor.getAdaptor(player);
                IItemList<IAEGasStack> list = inv.getAvailableItems(this.getChannel().createList());
                if (list.isEmpty()) {
                    playerInventory.setInventorySlotContents(playerInventory.currentItem, ItemStack.EMPTY);
                    ItemStack extraB = ia.addItems(this.core.copy());
                    if (!extraB.isEmpty()) {
                        player.dropItem(extraB, false);
                    }
                    IItemHandler upgradesInventory = this.getUpgradesInventory(stack);
                    for (int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSlots(); ++upgradeIndex) {
                        ItemStack upgradeStack = upgradesInventory.getStackInSlot(upgradeIndex);
                        ItemStack leftStack = ia.addItems(upgradeStack);
                        if (!leftStack.isEmpty() && upgradeStack.getItem() instanceof IUpgradeModule) {
                            player.dropItem(upgradeStack, false);
                        }
                    }
                    ItemStack extraA = ia.addItems(this.casing.copy());
                    if (!extraA.isEmpty()) {
                        player.dropItem(extraA, false);
                    }
                    if (player.inventoryContainer != null) {
                        player.inventoryContainer.detectAndSendChanges();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        return GuiText.StorageCells.getUnlocalized();
    }

}
