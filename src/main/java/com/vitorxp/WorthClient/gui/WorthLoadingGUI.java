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

    private long animStart;
    private boolean closing = false;
    private final int ANIM_TIME = 700;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
        this.animStart = System.currentTimeMillis();
    }

    public void update(String newText, float newProgress) {
        this.text = newText;
        this.progress = newProgress;
    }

    public void triggerClose() {
        this.closing = true;
        this.animStart = System.currentTimeMillis();
    }

    @Override
    public void updateScreen() {
        long elapsed = System.currentTimeMillis() - animStart;
        if (closing && elapsed >= ANIM_TIME) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        float alpha = getAnimationAlpha();

        drawBackgroundFancy(alpha);
        drawLogo(alpha);
        drawProgressBar(alpha);
        drawLoadingText(alpha);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private float getAnimationAlpha() {
        long elapsed = System.currentTimeMillis() - animStart;
        float anim = Math.min(1f, elapsed / (float) ANIM_TIME);
        return closing ? 1f - anim : anim;
    }

    private void drawBackgroundFancy(float alpha) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

        drawRect(0, 0, width, height, new Color(0, 0, 0, (int) (140 * alpha)).getRGB());

        drawGradientRect(0, 0, width, height,
                new Color(30, 30, 40, (int) (120 * alpha)).getRGB(),
                new Color(10, 10, 18, (int) (200 * alpha)).getRGB());
    }

    private void drawLogo(float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        float floating = (float) Math.sin(System.currentTimeMillis() / 600.0) * 4f;

        mc.getTextureManager().bindTexture(LOGO);

        int w = 340 / 2;
        int h = 300 / 2;
        int x = width / 2 - w / 2;
        int y = height / 2 - 80 + (int) floating;

        GlStateManager.color(1, 1, 1, alpha);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);

        GlStateManager.popMatrix();
    }

    private void drawLoadingText(float alpha) {
        int c = new Color(220, 220, 220, (int)(255 * alpha)).getRGB();

        drawCenteredString(fontRendererObj, text, width / 2, height / 2 + 70, c);
    }

    private void drawProgressBar(float alpha) {
        int barWidth = 250;
        int barHeight = 10;
        int x = width / 2 - barWidth / 2;
        int y = height / 2 + 50;

        drawRect(x, y, x + barWidth, y + barHeight,
                new Color(40, 40, 50, (int)(200 * alpha)).getRGB());

        int fill = (int) (barWidth * progress);

        Color neon = new Color(0, 170, 255, (int)(255 * alpha));

        drawRect(x, y, x + fill, y + barHeight, neon.getRGB());

        drawGradientRect(x, y - 3, x + fill, y,
                new Color(0, 170, 255, (int)(120 * alpha)).getRGB(),
                new Color(0, 170, 255, 0).getRGB());
    }
}