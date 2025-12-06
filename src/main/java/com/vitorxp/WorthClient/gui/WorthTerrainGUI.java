package com.vitorxp.WorthClient.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;

public class WorthTerrainGUI extends GuiScreen {

    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");
    private static final ResourceLocation BACKGROUND = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    private long initTime;

    public WorthTerrainGUI() {
        this.initTime = System.currentTimeMillis();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawBackgroundFancy();
        drawLogo();
        drawLoadingBar();
        drawText();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawBackgroundFancy() {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

        drawRect(0, 0, width, height, new Color(0, 0, 0, 100).getRGB());

        drawGradientRect(0, 0, width, height,
                new Color(0, 0, 0, 0).getRGB(),
                new Color(0, 0, 0, 150).getRGB());
    }

    private void drawLogo() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        float floatOffset = (float) Math.sin((System.currentTimeMillis() % 2000) / 318.0) * 5.0f;

        this.mc.getTextureManager().bindTexture(LOGO);

        int w = 200;
        int h = 150;
        int x = width / 2 - w / 2;
        int y = height / 2 - 100 + (int) floatOffset;

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);

        GlStateManager.popMatrix();
    }

    private void drawLoadingBar() {
        int barWidth = 200;
        int barHeight = 6;
        int centerX = width / 2;
        int centerY = height / 2 + 60;
        int startX = centerX - barWidth / 2;

        drawRect(startX, centerY, startX + barWidth, centerY + barHeight, new Color(40, 40, 40, 200).getRGB());

        long time = System.currentTimeMillis();
        float speed = 0.002f;
        float animPos = (float) ((Math.sin(time * speed) + 1) / 2.0);

        int fillWidth = 60;
        int maxPos = barWidth - fillWidth;
        int currentPos = (int) (maxPos * animPos);

        int color1 = new Color(0, 170, 255).getRGB();
        int color2 = new Color(0, 100, 200).getRGB();

        drawGradientRect(startX + currentPos, centerY, startX + currentPos + fillWidth, centerY + barHeight, color1, color2);
    }

    private void drawText() {
        String txt = "Carregando Mundo...";

        long dotAnim = (System.currentTimeMillis() / 500) % 4;
        if (dotAnim == 0) txt = "Carregando Mundo";
        else if (dotAnim == 1) txt = "Carregando Mundo.";
        else if (dotAnim == 2) txt = "Carregando Mundo..";
        else txt = "Carregando Mundo...";

        int strWidth = this.fontRendererObj.getStringWidth(txt);
        this.fontRendererObj.drawStringWithShadow(txt, (width / 2) - (strWidth / 2), height / 2 + 75, 0xFFFFFF);
    }
}