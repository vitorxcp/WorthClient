package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LoadingScreenHook {

    public static LoadingScreenHook INSTANCE = new LoadingScreenHook();
    public static WorthLoadingGUI customGUI;

    private boolean forced = false;

    public static void inject() {
        if (customGUI == null)
            customGUI = new WorthLoadingGUI(Minecraft.getMinecraft());
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {

        if (event.gui instanceof GuiDownloadTerrain) {

            if (customGUI == null)
                customGUI = new WorthLoadingGUI(Minecraft.getMinecraft());

            event.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(customGUI);

            forced = true;
            return;
        }

        if (forced && event.gui == null) {
            event.setCanceled(true);
            forced = false;
        }
    }
}
