package com.mekeng.github.xmod.nae;

import co.neeve.nae2.NAE2;
import com.mekeng.github.MekEng;
import mekanism.common.capabilities.Capabilities;

public class NAEInit {

    public static void loadExposerHandler() {
        NAE2.api().exposer().registerHandler(MekEng.class, Capabilities.GAS_HANDLER_CAPABILITY, GasExposerHandler.class);
    }

}
