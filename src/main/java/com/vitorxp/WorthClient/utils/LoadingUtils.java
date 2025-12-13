package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class LoadingUtils {

    private static long lastUpdate = 0;

    public static void renderLoading(String text, float progress) {
        long now = System.currentTimeMillis();

        if (now - lastUpdate < 16) {
            return;
        }
        lastUpdate = now;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen instanceof WorthLoadingGUI) {
            WorthLoadingGUI gui = (WorthLoadingGUI) mc.currentScreen;

            gui.update(text, progress);

            ScaledResolution res = new ScaledResolution(mc);
            int scale = res.getScaleFactor();
            int w = mc.displayWidth / scale;
            int h = mc.displayHeight / scale;

            gui.drawScreen(w, h);

            mc.updateDisplay();
        }
    }
}