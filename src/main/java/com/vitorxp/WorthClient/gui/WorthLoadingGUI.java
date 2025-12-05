package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.awt.*;

public class WorthLoadingGUI extends GuiScreen {

    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_loading.png");

    public String text = "Carregando...";
    public float progress = 0f;

    private long startTime;
    private boolean closing = false;

    private final int ANIM_TIME = 700;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
        this.startTime = System.currentTimeMillis();
    }

    public void update(String newText, float newProgress) {
        this.text = newText;
        this.progress = newProgress;
    }

    public void triggerClose() {
        this.closing = true;
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void updateScreen() {
        long elapsed = System.currentTimeMillis() - startTime;
        float anim = Math.min(1f, (float) elapsed / ANIM_TIME);

        if (closing && anim >= 1f) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        float alpha = 1f;

        drawBackground(alpha);
        drawLogo(alpha);
        drawLoadingText(alpha);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawBackground(float alpha) {
        drawGradientRect(0, 0, width, height,
                new Color(255, 255, 255, (int)(255 * alpha)).getRGB(),
                new Color(255, 255, 255, (int)(255 * alpha)).getRGB());
    }

    private void drawLogo(float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();

        float pulse = (float) (Math.sin(System.currentTimeMillis() / 450.0) * 4);

        this.mc.getTextureManager().bindTexture(LOGO);

        int w = 340 / 2;
        int h = 300 / 2;

        int x = width / 2 - w / 2;
        int y = height / 2 - h / 2 + (int) pulse;

        GlStateManager.color(1, 1, 1, alpha);
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);

        GlStateManager.popMatrix();
    }

    private void drawLoadingText(float alpha) {
        int color = new Color(60, 60, 60, (int)(255 * alpha)).getRGB();

        drawCenteredString(fontRendererObj, text, width / 2, height / 2 + 70, color);
    }
}