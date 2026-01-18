package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.gui.WorthLoadingGUI;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public class LoadingUtils {

    private static String currentText = "Iniciando...";
    private static float currentProgress = 0.0f;

    public static void setCurrentText(String text) {
        currentText = text;
    }

    public static void setCurrentProgress(float progress) {
        currentProgress = progress;
    }

    public static String getCurrentText() { return currentText; }
    public static float getCurrentProgress() { return currentProgress; }

    public static void renderLoading(String text, float progress) {
        if (text != null) currentText = text;
        if (progress >= 0) currentProgress = progress;

        Minecraft mc = Minecraft.getMinecraft();

        if (!(mc.currentScreen instanceof WorthLoadingGUI)) {
            mc.displayGuiScreen(new WorthLoadingGUI());
        }

        if (mc.currentScreen instanceof WorthLoadingGUI) {
            WorthLoadingGUI gui = (WorthLoadingGUI) mc.currentScreen;

            gui.update(currentText, currentProgress);

            mc.getFramebuffer().bindFramebuffer(true);

            net.minecraft.client.gui.ScaledResolution res = new net.minecraft.client.gui.ScaledResolution(mc);
            int w = res.getScaledWidth();
            int h = res.getScaledHeight();

            gui.drawScreen(0, 0, 0);

            mc.updateDisplay();

            Display.sync(60);
        }
    }
}