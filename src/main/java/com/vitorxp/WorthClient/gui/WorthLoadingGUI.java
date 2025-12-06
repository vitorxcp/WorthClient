package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class WorthLoadingGUI extends GuiScreen {

    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");
    private static final ResourceLocation BACKGROUND = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    public String text = "Carregando...";
    public float progress = 0f;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
    }

    public void update(String newText, float newProgress) {
        this.text = newText;
        this.progress = newProgress;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float alpha = 1.0f;

        drawRect(0, 0, width, height, 0xFF101010);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            mc.getTextureManager().bindTexture(BACKGROUND);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
        } catch (Exception ignored) {}

        drawRect(0, 0, width, height, new Color(0, 0, 0, 100).getRGB());

        drawLogo();
        drawProgressBar(alpha);
        drawLoadingText(alpha);
    }

    private void drawLogo() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            mc.getTextureManager().bindTexture(LOGO);
            int w = 340 / 2;
            int h = 300 / 2;
            int x = width / 2 - w / 2;
            int y = height / 2 - 80;
            drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);
        } catch (Exception e) {
            drawRect(width/2 - 50, height/2 - 50, width/2 + 50, height/2 + 50, -1);
        }

        GlStateManager.popMatrix();
    }

    private void drawLoadingText(float alpha) {
        int c = new Color(220, 220, 220, 255).getRGB();
        String drawTxt = (text == null) ? "Carregando..." : text;
        drawCenteredString(fontRendererObj, drawTxt, width / 2, height / 2 + 70, c);
    }

    private void drawProgressBar(float alpha) {
        int barWidth = 250;
        int barHeight = 10;
        int x = width / 2 - barWidth / 2;
        int y = height / 2 + 50;

        drawRect(x, y, x + barWidth, y + barHeight, new Color(40, 40, 50, 255).getRGB());

        int fill = (int) (barWidth * progress);

        Color neon = new Color(0, 170, 255, 255);
        drawRect(x, y, x + fill, y + barHeight, neon.getRGB());
    }
}