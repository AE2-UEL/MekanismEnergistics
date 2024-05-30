package com.mekeng.github.common.container;

import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.config.Settings;
import appeng.container.guisync.GuiSync;
import appeng.util.Platform;
import com.mekeng.github.common.me.inventory.IGasInventory;
import com.mekeng.github.common.part.PartGasLevelEmitter;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerGasLevelEmitter extends ContainerGasConfigurable<PartGasLevelEmitter> {

    @SideOnly(Side.CLIENT)
    private GuiTextField textField;
    @GuiSync(3)
    public long EmitterValue = -1;

    public ContainerGasLevelEmitter(final InventoryPlayer ip, final PartGasLevelEmitter te) {
        super(ip, te);
        this.holder.put("set_level", o -> this.setLevel(o.get(0)));
    }

    @SideOnly(Side.CLIENT)
    public void setTextField(final GuiTextField level) {
        this.textField = level;
        this.textField.setText(String.valueOf(this.EmitterValue));
    }

    public void setLevel(final long l) {
        this.getUpgradeable().setReportingValue(l);
        this.EmitterValue = l;
    }

    @Override
    protected void setupConfig() {
        // NO-OP
    }

    @Override
    protected boolean supportCapacity() {
        return false;
    }

    @Override
    public int availableUpgrades() {
        return 0;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        if (Platform.isServer()) {
            this.EmitterValue = this.getUpgradeable().getReportingValue();
            this.setRedStoneMode((RedstoneMode) this.getUpgradeable().getConfigManager().getSetting(Settings.REDSTONE_EMITTER));
        }
        this.standardDetectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        if (field.equals("EmitterValue")) {
            System.out.print(oldValue + " " + newValue + " " + this.EmitterValue + "\n");
            if (this.textField != null) {
                this.textField.setText(String.valueOf(this.EmitterValue));
            }
        }
    }

    @Override
    public IGasInventory getGasConfigInventory() {
        return this.getUpgradeable().getConfig();
    }
}
