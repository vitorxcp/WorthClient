// Classe de configuração para HUD de teclas e CPS
package com.vitorxp.SkyBlockModVX.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;

import java.awt.Color;
import java.io.*;

public class KeystrokesColors {
    public static Color backgroundDefault = new Color(0, 0, 0, 120);
    public static Color backgroundPressed = new Color(50, 150, 255, 180);
    public static Color border = new Color(255, 255, 255, 80);
    public static Color text = Color.WHITE;
    public static Color cpsText = new Color(170, 170, 170);

    private static final File configFile = new File(Minecraft.getMinecraft().mcDataDir, "SkyBlockModVX/keystrokes_colors.json");

    public static void setBackgroundDefault(Color color) {
        backgroundDefault = color;
    }

    public static void setBackgroundPressed(Color color) {
        backgroundPressed = color;
    }

    public static void setBorder(Color color) {
        border = color;
    }

    public static void setText(Color color) {
        text = color;
    }

    public static void setCpsText(Color color) {
        cpsText = color;
    }

    public static int getBackground(boolean pressed) {
        return (pressed ? backgroundPressed : backgroundDefault).getRGB();
    }

    public static int getBorder() {
        return border.getRGB();
    }

    public static int getText() {
        return text.getRGB();
    }

    public static int getCpsText() {
        return cpsText.getRGB();
    }

    public static void saveColors() {
        try {
            if (!configFile.getParentFile().exists()) configFile.getParentFile().mkdirs();

            JsonObject json = new JsonObject();
            json.addProperty("backgroundDefault", colorToHex(backgroundDefault));
            json.addProperty("backgroundPressed", colorToHex(backgroundPressed));
            json.addProperty("border", colorToHex(border));
            json.addProperty("text", colorToHex(text));
            json.addProperty("cpsText", colorToHex(cpsText));

            try (FileWriter writer = new FileWriter(configFile)) {
                writer.write(json.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadColors() {
        if (!configFile.exists()) return;

        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();

            backgroundDefault = hexToColor(json.get("backgroundDefault").getAsString());
            backgroundPressed = hexToColor(json.get("backgroundPressed").getAsString());
            border = hexToColor(json.get("border").getAsString());
            text = hexToColor(json.get("text").getAsString());
            cpsText = hexToColor(json.get("cpsText").getAsString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void resetToDefault() {
        backgroundDefault = new Color(0, 0, 0, 120);
        backgroundPressed = new Color(50, 150, 255, 180);
        border = new Color(255, 255, 255, 80);
        text = Color.WHITE;
        cpsText = new Color(170, 170, 170);
        saveColors();
    }

    private static String colorToHex(Color color) {
        return String.format("#%08X", color.getRGB());
    }

    private static Color hexToColor(String hex) {
        return new Color((int) Long.parseLong(hex.replace("#", ""), 16), true);
    }
}
