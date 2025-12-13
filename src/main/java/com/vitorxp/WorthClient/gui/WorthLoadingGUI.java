package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.utils.GuiTextureLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class WorthLoadingGUI extends GuiScreen {

    private final Minecraft mc;
    private static ResourceLocation LOGO_LOC;
    private static ResourceLocation BG_LOC;
    private static final int BG_COLOR = new Color(10, 10, 10).getRGB();
    private static final int BAR_BG_COLOR = new Color(55, 50, 25).getRGB();
    private static final int BAR_FILL_COLOR = new Color(255, 170, 0).getRGB();
    private static final int TEXT_COLOR = new Color(255, 255, 240).getRGB();
    public String text = "Carregando...";
    public float progress = 0f;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
    }

    private void initTextures() {
        if (LOGO_LOC != null) return;
        try {
            if (mc.getTextureManager() != null) {
                LOGO_LOC = GuiTextureLoader.load("textures/gui/logo_splash.png");
                BG_LOC = GuiTextureLoader.load("textures/gui/Background_3.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(String newText, float newProgress) {
        this.text = newText;
        if (newProgress > this.progress) this.progress = newProgress;
    }

    public void drawScreen(int width, int height) {
        initTextures();
        drawRect(0, 0, width, height, BG_COLOR);

        if (BG_LOC != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(BG_LOC);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

            drawRect(0, 0, width, height, 0xCC000000);
        }

        int centerX = width / 2;
        int logoSize = 100;
        int barWidth = 220;
        int barHeight = 10;
        int totalContentHeight = logoSize + 10 + 10 + 15 + barHeight;
        int startY = (height - totalContentHeight) / 2;
        int logoY = startY + (logoSize / 2);
        int textY = startY + logoSize + 10;
        int barY = textY + 20;

        drawLogo(centerX, logoY, logoSize);
        drawStatusText(centerX, textY);
        drawRoundedGoldBar(centerX, barY, barWidth, barHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawScreen(sr.getScaledWidth(), sr.getScaledHeight());
    }

    private void drawLogo(int x, int y, int size) {
        if (LOGO_LOC == null) return;

        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.translate(-size / 2.0f, -size / 2.0f, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        mc.getTextureManager().bindTexture(LOGO_LOC);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, size, size, size, size);
        GlStateManager.popMatrix();
    }

    private void drawRoundedGoldBar(int centerX, int y, int width, int height) {
        int left = centerX - (width / 2);
        int right = left + width;
        int bottom = y + height;

        drawRect(left + 2, y, right - 2, bottom, BAR_BG_COLOR);
        drawRect(left, y + 1, left + 2, bottom - 1, BAR_BG_COLOR);
        drawRect(right - 2, y + 1, right, bottom - 1, BAR_BG_COLOR);

        int fillWidth = (int) (width * progress);
        if (fillWidth > width) fillWidth = width;
        if (fillWidth > 4) {
            int fillRight = left + fillWidth;

            drawRect(left + 2, y, fillRight - 2, bottom, BAR_FILL_COLOR);
            drawRect(left, y + 1, left + 2, bottom - 1, BAR_FILL_COLOR);

            if (fillWidth >= width - 2) {
                drawRect(fillRight - 2, y + 1, fillRight, bottom - 1, BAR_FILL_COLOR);
            } else {
                drawRect(fillRight - 2, y + 1, fillRight, bottom - 1, BAR_FILL_COLOR);
            }
        }
    }

    private void drawStatusText(int centerX, int y) {
        if (mc.fontRendererObj == null) return;
        GlStateManager.enableTexture2D();

        String status = text;
        int strW = mc.fontRendererObj.getStringWidth(status);

        mc.fontRendererObj.drawStringWithShadow(status, centerX - (strW / 2), y, TEXT_COLOR);
    }
}