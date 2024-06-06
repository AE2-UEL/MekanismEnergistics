package com.mekeng.github.common.container;

import appeng.api.config.SecurityPermissions;
import appeng.api.config.Upgrades;
import appeng.api.util.IConfigManager;
import appeng.container.guisync.GuiSync;
import appeng.helpers.InventoryAction;
import appeng.util.IConfigManagerHost;
import appeng.util.Platform;
import com.mekeng.github.common.me.data.IAEGasStack;
import com.mekeng.github.common.me.duality.IGasInterfaceHost;
import com.mekeng.github.common.me.duality.impl.DualityGasInterface;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.util.Utils;
import com.mekeng.github.util.helpers.GasSyncHelper;
import com.mekeng.github.util.helpers.ItemGasHandler;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;

public class ContainerGasInterface extends ContainerGasConfigurable<IGasInterfaceHost> implements IConfigManagerHost {
    private final DualityGasInterface myDuality;
    private final GasSyncHelper tankSync;
    private IConfigManagerHost gui;
    // Holds the gas the client wishes to extract, or null for insert
    private IAEGasStack clientRequestedTargetGas = null;

    @GuiSync(7)
    public int capacityUpgrades = 0;

    public ContainerGasInterface(final InventoryPlayer ip, final IGasInterfaceHost te) {
        super(ip, te);

        this.myDuality = te.getDualityGasInterface();
        this.tankSync = GasSyncHelper.create(this.myDuality.getTanks(), DualityGasInterface.NUMBER_OF_TANKS);
        this.holder.put("set_target", o -> this.setTargetGasStack(o.get(0)));
    }

    @Override
    protected int getHeight() {
        return 231;
    }

    @Override
    public IGasInventory getGasConfigInventory() {
        return this.getUpgradeable().getDualityGasInterface().getConfig();
    }

    @Override
    public void updateSetting(IConfigManager manager, Enum settingName, Enum newValue) {
        if (this.getGui() != null) {
            this.getGui().updateSetting(manager, settingName, newValue);
        }
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);

        if (Platform.isServer()) {
            this.tankSync.sendDiff(this.listeners);
            if (capacityUpgrades != getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY)) {
                capacityUpgrades = getUpgradeable().getInstalledUpgrades(Upgrades.CAPACITY);
            }
        }

        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("capacityUpgrades")) {
            this.capacityUpgrades = (int) newValue;
            this.myDuality.getTanks().setCap((int) (Math.pow(4, this.capacityUpgrades + 1) * 1000));
        }
    }

    @Override
    protected void setupConfig() {
        this.setupUpgrades();
    }

    @Override
    protected void loadSettingsFromHost(final IConfigManager cm) {
    }

    @Override
    public void addListener(@Nonnull IContainerListener listener) {
        super.addListener(listener);
        this.tankSync.sendFull(Collections.singleton(listener));
    }

    @Override
    public void receiveGasSlots(Map<Integer, IAEGasStack> gases) {
        super.receiveGasSlots(gases);
        // Prevent cheat packet.
        if (Platform.isClient()) {
            this.tankSync.readPacket(gases);
        }
    }

    private IConfigManagerHost getGui() {
        return this.gui;
    }

    public void setGui(@Nonnull final IConfigManagerHost gui) {
        this.gui = gui;
    }

    public void doAction(EntityPlayerMP player, InventoryAction action, int slot, long id) {
        if (action != InventoryAction.FILL_ITEM && action != InventoryAction.EMPTY_ITEM) {
            super.doAction(player, action, slot, id);
            return;
        }

        final ItemStack held = player.inventory.getItemStack();
        ItemStack heldCopy = held.copy();
        heldCopy.setCount(1);
        ItemGasHandler gh = Utils.getItemGasHandler(heldCopy);
        if (gh == null) {
            // only gas handlers items
            return;
        }

        if (action == InventoryAction.FILL_ITEM && this.clientRequestedTargetGas != null) {
            final IAEGasStack stack = this.clientRequestedTargetGas.copy();

            // Check how much we can store in the item
            stack.setStackSize(Integer.MAX_VALUE);
            int amountAllowed = gh.addGas(stack.getGasStack(), false);
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedGasContainer = held.copy();
                copiedGasContainer.setCount(1);
                gh = Utils.getItemGasHandler(copiedGasContainer);

                GasStack extractableGas = this.myDuality.getTankHandler().drawGas(null, stack.setStackSize(amountAllowed).getGasStack(), false);
                if (extractableGas == null || extractableGas.amount == 0) {
                    break;
                }

                int fillableAmount = gh.addGas(extractableGas, false);
                if (fillableAmount > 0) {
                    GasStack extractedGas = this.myDuality.getTankHandler().drawGas(null, extractableGas, true);
                    gh.addGas(extractedGas, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(gh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(gh.getContainer())) {
                        player.dropItem(gh.getContainer(), false);
                    }
                }
            }
        } else if (action == InventoryAction.EMPTY_ITEM) {
            int heldAmount = held.getCount();
            for (int i = 0; i < heldAmount; i++) {
                ItemStack copiedGasContainer = held.copy();
                copiedGasContainer.setCount(1);
                gh = Utils.getItemGasHandler(copiedGasContainer);

                GasStack drainable = gh.removeGas(this.myDuality.getTanks().getTanks()[slot].getMaxGas(), false);
                if (drainable != null) {
                    gh.removeGas(drainable, true);
                    this.myDuality.getTankHandler().receiveGas(null, drainable, true);
                }

                if (held.getCount() == 1) {
                    player.inventory.setItemStack(gh.getContainer());
                } else {
                    player.inventory.getItemStack().shrink(1);
                    if (!player.inventory.addItemStackToInventory(gh.getContainer())) {
                        player.dropItem(gh.getContainer(), false);
                    }
                }
            }
        }
        this.updateHeld(player);
    }

    public void setTargetGasStack(final IAEGasStack stack) {
        if (Platform.isClient()) {
            if (stack == null && this.clientRequestedTargetGas == null) {
                return;
            }
            if (stack != null && this.clientRequestedTargetGas != null && stack.getGasStack().isGasEqual(this.clientRequestedTargetGas.getGasStack())) {
                return;
            }
        }
        this.clientRequestedTargetGas = stack == null ? null : stack.copy();
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 2;
    }

}
