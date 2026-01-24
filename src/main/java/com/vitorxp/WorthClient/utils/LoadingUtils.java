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

    public static String getCurrentText() {
        return currentText;
    }

    public static float getCurrentProgress() {
        return currentProgress;
    }

    public static void renderLoading(String text, float progress) {
        if (text != null) currentText = text;
        if (progress >= 0) currentProgress = progress;

        Minecraft mc = Minecraft.getMinecraft();
        WorthLoadingGUI gui;
        if (mc.currentScreen instanceof WorthLoadingGUI) {
            gui = (WorthLoadingGUI) mc.currentScreen;
        } else {
            gui = new WorthLoadingGUI();
        }
        try {
            gui.drawProgress(currentText, currentProgress);
            Display.update();
        } catch (Exception ignored) {
        }
    }
}