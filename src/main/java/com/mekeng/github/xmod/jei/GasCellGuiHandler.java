package com.mekeng.github.xmod.jei;

import appeng.client.gui.implementations.GuiCellWorkbench;
import appeng.container.slot.SlotFake;
import appeng.fluids.client.gui.widgets.GuiFluidSlot;
import com.mekeng.github.common.ItemAndBlocks;
import mekanism.api.gas.GasStack;
import mezz.jei.api.gui.IGhostIngredientHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class GasCellGuiHandler implements IGhostIngredientHandler<GuiCellWorkbench> {

    public static final GasCellGuiHandler INSTANCE = new GasCellGuiHandler();

    private GasCellGuiHandler() {
        // NO-OP
    }

    @Nonnull
    @Override
    public <I> List<Target<I>> getTargets(@Nonnull GuiCellWorkbench gui, @Nonnull I ingredient, boolean doStart) {
        ArrayList targets = new ArrayList<>();
        if (ingredient instanceof GasStack) {
            ItemStack stack = new ItemStack(ItemAndBlocks.DUMMY_GAS);
            ItemAndBlocks.DUMMY_GAS.setGasStack(stack, (GasStack) ingredient);
            targets.addAll(gui.getPhantomTargets(stack));
        } else {
            targets.addAll(gui.getPhantomTargets(ingredient));
        }
        if (doStart && GuiScreen.isShiftKeyDown() && Mouse.isButtonDown(0)) {
            for (Object o : targets) {
                Target target = (Target) o;
                if (gui.getFakeSlotTargetMap().get(target) instanceof SlotFake) {
                    if (((SlotFake) gui.getFakeSlotTargetMap().get(target)).getStack().isEmpty()) {
                        target.accept(ingredient);
                        break;
                    }
                } else if (gui.getFakeSlotTargetMap().get(target) instanceof GuiFluidSlot) {
                    if (((GuiFluidSlot) gui.getFakeSlotTargetMap().get(target)).getFluidStack() == null) {
                        target.accept(ingredient);
                        break;
                    }
                }
            }
        }
        return targets;
    }

    @Override
    public void onComplete() {

    }
}
