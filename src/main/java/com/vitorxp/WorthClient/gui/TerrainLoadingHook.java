package com.vitorxp.WorthClient.gui;

import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TerrainLoadingHook {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiDownloadTerrain) {
            event.gui = new WorthTerrainGUI();
        }
    }
}