package com.mekeng.github.common.me.inventory;

public interface IGasInventoryHost {

    void onGasInventoryChanged(IGasInventory inv, int slot);

    static IGasInventoryHost empty() {
        return new EmptyHost();
    }

    final class EmptyHost implements IGasInventoryHost {

        private EmptyHost() {
            // NO-OP
        }

        @Override
        public void onGasInventoryChanged(IGasInventory inv, int slot) {

        }
    }

}
