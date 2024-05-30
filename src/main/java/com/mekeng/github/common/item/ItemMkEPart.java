package com.mekeng.github.common.item;

import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.items.AEBaseItem;
import com.mekeng.github.common.part.IPartGroup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

public class ItemMkEPart<T extends IPart> extends AEBaseItem implements IPartItem<T>, IItemGroup {

    private final Function<ItemStack, T> factory;

    public ItemMkEPart(Function<ItemStack, T> factory) {
        this.factory = factory;
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(@Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(hand), pos, side, player, hand, world);
    }

    @Nullable
    @Override
    public T createPartFromItemStack(ItemStack stack) {
        return this.factory.apply(stack);
    }

    @Override
    public String getUnlocalizedGroupName(Set<ItemStack> otherItems, ItemStack is) {
        IPart obj = this.factory.apply(is);
        if (obj instanceof IPartGroup) {
            return ((IPartGroup) obj).getUnlocalizedGroupName();
        }
        return null;
    }
}
