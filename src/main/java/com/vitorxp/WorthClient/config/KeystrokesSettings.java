package com.vitorxp.WorthClient.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import java.awt.Color;
import java.io.*;

public class KeystrokesSettings {

    public static boolean enabled = true;
    public static boolean showClicks = true;
    public static boolean showMovement = true;
    public static boolean showSpace = true;
    public static boolean useArrows = false;
    public static boolean textShadow = false;
    public static boolean borderEnabled = false;
    public static float scale = 1.0f;
    public static float boxSize = 20.0f;
    public static float borderThickness = 1.0f;
    public static Color backgroundDefault = new Color(0, 0, 0, 120);
    public static Color backgroundPressed = new Color(255, 255, 255, 100);
    public static Color borderColor = new Color(0, 0, 0, 255);
    public static Color textColor = Color.WHITE;
    public static Color textPressedColor = Color.BLACK;
    public static Color cpsTextColor = new Color(170, 170, 170);
    public static boolean chromaMode = false;
    public static double chromaSpeed = 2.0;

    public static int getBackgroundColor(boolean pressed) {
        if (pressed) return backgroundPressed.getRGB();
        return backgroundDefault.getRGB();
    }

    public static int getTextColor(boolean pressed) {
        if (chromaMode) return getChromaColor(0);
        return pressed ? textPressedColor.getRGB() : textColor.getRGB();
    }

    public static int getBorderColor() {
        if (chromaMode) return getChromaColor(1000);
        return borderColor.getRGB();
    }

    public static int getChromaColor(long offset) {
        float hue = (float) (System.nanoTime() + offset * 1000000) / (float) (1000000000L / chromaSpeed);
        return Color.getHSBColor(hue % 1f, 1.0f, 1.0f).getRGB();
    }
}