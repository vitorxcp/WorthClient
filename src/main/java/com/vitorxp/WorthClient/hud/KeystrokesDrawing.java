package com.vitorxp.WorthClient.hud;

import com.vitorxp.WorthClient.config.KeystrokesSettings;
import com.vitorxp.WorthClient.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class KeystrokesDrawing {

    public static void drawKey(String keyChar, int x, int y, boolean pressed) {
        float size = KeystrokesSettings.boxSize;
        drawRect(x, y, size, size, pressed, keyChar);
    }

    public static void drawRect(int x, int y, float width, float height, boolean pressed, String label) {
        int bgColor = KeystrokesSettings.getBackgroundColor(pressed);
        int borderColor = KeystrokesSettings.getBorderColor();
        int textColor = KeystrokesSettings.getTextColor(pressed);

        RenderUtil.drawRect(x, y, x + (int)width, y + (int)height, bgColor);

        if (KeystrokesSettings.borderEnabled) {
            float thickness = KeystrokesSettings.borderThickness;

            RenderUtil.drawRect(x, y, x + (int)width, y + (int)thickness, borderColor);
            RenderUtil.drawRect(x, y + (int)height - (int)thickness, x + (int)width, y + (int)height, borderColor);
            RenderUtil.drawRect(x, y, x + (int)thickness, y + (int)height, borderColor);
            RenderUtil.drawRect(x + (int)width - (int)thickness, y, x + (int)width, y + (int)height, borderColor);
        }

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        String textToDraw = label;

        if (KeystrokesSettings.useArrows) {
            if (label.equalsIgnoreCase("W")) textToDraw = "^";
            else if (label.equalsIgnoreCase("A")) textToDraw = "<";
            else if (label.equalsIgnoreCase("S")) textToDraw = "v";
            else if (label.equalsIgnoreCase("D")) textToDraw = ">";
        }

        int textX = x + ((int)width - fr.getStringWidth(textToDraw)) / 2;
        int textY = y + ((int)height - fr.FONT_HEIGHT) / 2;

        if (KeystrokesSettings.textShadow) {
            fr.drawStringWithShadow(textToDraw, textX, textY, textColor);
        } else {
            fr.drawString(textToDraw, textX, textY, textColor);
        }
    }

    public static void drawCps(String label, int cps, int x, int y, boolean pressed, int width, int height) {
        if (!KeystrokesSettings.showClicks) return;

        drawRect(x, y, width, height, pressed, "");

        FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
        int textColor = KeystrokesSettings.getTextColor(pressed);

        int labelW = fr.getStringWidth(label);
        fr.drawString(label, x + (width - labelW) / 2, y + height / 2 - 4, textColor);

        String cpsTxt = cps + " CPS";
        GlStateManager.pushMatrix();
        float scale = 0.6f;
        GlStateManager.scale(scale, scale, 1);
        int cpsX = (int)((x + width / 2) / scale) - fr.getStringWidth(cpsTxt) / 2;
        int cpsY = (int)((y + height - 6) / scale);
        fr.drawString(cpsTxt, cpsX, cpsY, KeystrokesSettings.chromaMode ? KeystrokesSettings.getChromaColor(500) : 0xFFAAAAAA);
        GlStateManager.popMatrix();
    }
}