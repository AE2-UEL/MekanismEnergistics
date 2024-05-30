package com.mekeng.github.xmod.jei;

import appeng.client.gui.implementations.GuiCellWorkbench;
import com.mekeng.github.common.ItemAndBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@JEIPlugin
@ParametersAreNonnullByDefault
public class MkEJei implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IIngredientBlacklist blacklist = registry.getJeiHelpers().getIngredientBlacklist();
        blacklist.addIngredientToBlacklist(new ItemStack(ItemAndBlocks.DUMMY_GAS));
        registry.addGhostIngredientHandler(GuiCellWorkbench.class, GasCellGuiHandler.INSTANCE);
    }

}
