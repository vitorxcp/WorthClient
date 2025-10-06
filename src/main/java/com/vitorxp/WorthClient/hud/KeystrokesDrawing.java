package com.vitorxp.WorthClient.hud;

import com.vitorxp.WorthClient.config.KeystrokesColors;
import com.vitorxp.WorthClient.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class KeystrokesDrawing {

    private static void drawBorderedRect(int x, int y, int width, int height, int borderColor, int insideColor) {
        RenderUtil.drawRect(x, y, x + width, y + height, insideColor);
        RenderUtil.drawRect(x, y, x + width, y + 1, borderColor); // Top
        RenderUtil.drawRect(x, y + height - 1, x + width, y + height, borderColor); // Bottom
        RenderUtil.drawRect(x, y + 1, x + 1, y + height - 1, borderColor); // Left
        RenderUtil.drawRect(x + width - 1, y + 1, x + width, y + height - 1, borderColor); // Right
    }

    public static void drawKey(String label, int x, int y, boolean pressed, int width, int height) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int bgColor = KeystrokesColors.getBackground(pressed);
        int borderColor = KeystrokesColors.getBorder();
        int textColor = KeystrokesColors.getText();

        drawBorderedRect(x, y, width, height, borderColor, bgColor);

        int textX = x + (width - fontRenderer.getStringWidth(label)) / 2;
        int textY = y + (height - fontRenderer.FONT_HEIGHT) / 2;
        fontRenderer.drawStringWithShadow(label, textX, textY, textColor);
    }

    public static void drawCpsKey(String label, int cps, int x, int y, boolean pressed, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontRenderer = mc.fontRendererObj;
        int bgColor = KeystrokesColors.getBackground(pressed);
        int borderColor = KeystrokesColors.getBorder();

        drawBorderedRect(x, y, width, height, borderColor, bgColor);

        int labelWidth = fontRenderer.getStringWidth(label);
        int labelX = x + (width - labelWidth) / 2;
        int labelY = y + 3;
        fontRenderer.drawStringWithShadow(label, labelX, labelY, KeystrokesColors.getText());

        String cpsText = cps + " CPS";
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + width / 2.0f, y + height - 6, 0);
        GlStateManager.scale(0.7f, 0.7f, 1f);
        int cpsWidth = fontRenderer.getStringWidth(cpsText);
        fontRenderer.drawStringWithShadow(cpsText, -cpsWidth / 2, 0, KeystrokesColors.getCpsText());
        GlStateManager.popMatrix();
    }
}