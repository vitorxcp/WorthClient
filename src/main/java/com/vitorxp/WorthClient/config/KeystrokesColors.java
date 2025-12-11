package com.vitorxp.WorthClient.config;

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
    public static boolean chromaBackground = false;
    public static boolean chromaBorder = false;
    public static boolean chromaText = false;
    public static int chromaSpeed = 4000;
    private static final File configFile = new File(Minecraft.getMinecraft().mcDataDir, "WorthClient/keystrokes_colors.json");
    public static void setBackgroundDefault(Color color) { backgroundDefault = color; }
    public static void setBackgroundPressed(Color color) { backgroundPressed = color; }
    public static void setBorder(Color color) { border = color; }
    public static void setText(Color color) { text = color; }
    public static void setCpsText(Color color) { cpsText = color; }

    public static int getBackground(boolean pressed) {
        if (chromaBackground) {
            return getRainbowColor(0);
        }
        return (pressed ? backgroundPressed : backgroundDefault).getRGB();
    }

    public static int getBorder() {
        if (chromaBorder) {
            return getRainbowColor(1000);
        }
        return border.getRGB();
    }

    public static int getText() {
        if (chromaText) {
            return getRainbowColor(200);
        }
        return text.getRGB();
    }

    public static int getCpsText() {
        if (chromaText) {
            return getRainbowColor(400);
        }
        return cpsText.getRGB();
    }

    public static int getRainbowColor(int offset) {
        float hue = (float) ((System.currentTimeMillis() + offset) % chromaSpeed) / chromaSpeed;
        return Color.getHSBColor(hue, 0.9f, 1.0f).getRGB();
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
            json.addProperty("chromaBackground", chromaBackground);
            json.addProperty("chromaBorder", chromaBorder);
            json.addProperty("chromaText", chromaText);

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

            if (json.has("backgroundDefault")) backgroundDefault = hexToColor(json.get("backgroundDefault").getAsString());
            if (json.has("backgroundPressed")) backgroundPressed = hexToColor(json.get("backgroundPressed").getAsString());
            if (json.has("border")) border = hexToColor(json.get("border").getAsString());
            if (json.has("text")) text = hexToColor(json.get("text").getAsString());
            if (json.has("cpsText")) cpsText = hexToColor(json.get("cpsText").getAsString());
            if (json.has("chromaBackground")) chromaBackground = json.get("chromaBackground").getAsBoolean();
            if (json.has("chromaBorder")) chromaBorder = json.get("chromaBorder").getAsBoolean();
            if (json.has("chromaText")) chromaText = json.get("chromaText").getAsBoolean();

        } catch (Exception e) {
            e.printStackTrace();
            resetToDefault();
        }
    }

    public static void resetToDefault() {
        backgroundDefault = new Color(0, 0, 0, 120);
        backgroundPressed = new Color(50, 150, 255, 180);
        border = new Color(255, 255, 255, 80);
        text = Color.WHITE;
        cpsText = new Color(170, 170, 170);

        chromaBackground = false;
        chromaBorder = false;
        chromaText = false;

        saveColors();
    }

    private static String colorToHex(Color color) {
        return String.format("#%08X", color.getRGB());
    }

    private static Color hexToColor(String hex) {
        try {
            return new Color((int) Long.parseLong(hex.replace("#", ""), 16), true);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
}