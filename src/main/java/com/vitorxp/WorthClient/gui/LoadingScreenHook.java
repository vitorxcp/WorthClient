package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

public class LoadingScreenHook {

    private static VXLoadingScreenRenderer customRenderer;

    public static void inject() {
        try {
            Minecraft mc = Minecraft.getMinecraft();

            if (customRenderer == null)
                customRenderer = new VXLoadingScreenRenderer(mc);

            Field loadingField = Minecraft.class.getDeclaredField("loadingScreen");
            loadingField.setAccessible(true);

            VXLoadingScreenOverride override = new VXLoadingScreenOverride(mc, customRenderer);
            loadingField.set(mc, override);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent e) {
        if (e.gui instanceof GuiDownloadTerrain || e.gui == null)
            inject();
    }
}