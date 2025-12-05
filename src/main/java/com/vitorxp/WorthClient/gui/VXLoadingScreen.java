package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class VXLoadingScreen {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final ResourceLocation BACKGROUND = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");

    private static float fakeProgress = 0f;
    private static long startTime = System.currentTimeMillis();

    public static void draw() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        float time = (System.currentTimeMillis() - startTime) / 1000f;
        float ease = AnimationUtil.easeOutCubic(Math.min(fakeProgress + 0.3f, 1.0f));

        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        drawBackground(w, h);
        drawFloatingLogo(w, h, ease, time);
        drawProgressBar(w, h, ease);
        drawLoadingText(w, h, ease);
        drawSlidingBarsTransition(w, h, 1f - ease);

        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
    }

    private static void drawBackground(int w, int h) {
        mc.getTextureManager().bindTexture(BACKGROUND);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, w, h, w, h);

        Gui.drawRect(0, 0, w, h, new Color(10, 10, 15, 130).getRGB());
    }

    private static void drawFloatingLogo(int w, int h, float alpha, float time) {

        float yOffset = (float) Math.sin(time * 2) * 5f;

        int lw = 470 / 3;
        int lh = 97 / 3;

        int x = w / 2 - lw / 2;
        int y = h / 2 - lh - 20;

        GlStateManager.pushMatrix();
        GlStateManager.color(1f, 1f, 1f, alpha);

        mc.getTextureManager().bindTexture(LOGO);
        Gui.drawModalRectWithCustomSizedTexture(x, (int) (y + yOffset), 0, 0, lw, lh, lw, lh);

        GlStateManager.popMatrix();
    }

    private static void drawProgressBar(int w, int h, float ease) {

        int barWidth = 260;
        int barHeight = 8;
        int x1 = w / 2 - barWidth / 2;
        int y1 = h / 2 + 40;
        int fill = (int) (barWidth * fakeProgress);

        Gui.drawRect(x1, y1, x1 + barWidth, y1 + barHeight, new Color(30, 30, 40, 180).getRGB());
        Gui.drawRect(x1, y1, x1 + fill, y1 + barHeight, new Color(0, 150, 255, 220).getRGB());
    }

    private static void drawLoadingText(int w, int h, float alpha) {
        String msg = "Carregando o WorthClient...";
        int color = new Color(1f, 1f, 1f, alpha).getRGB();

        mc.fontRendererObj.drawString(
                msg,
                w / 2 - mc.fontRendererObj.getStringWidth(msg) / 2,
                h / 2 + 55,
                color
        );
    }

    private static void drawSlidingBarsTransition(int w, int h, float prog) {
        if (prog <= 0) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Color dark = new Color(139, 105, 20);
        Color light = new Color(255, 215, 0);

        int bars = 3;
        float barH = h / bars;
        float totalW = w + 200;

        for (int i = 0; i < bars; i++) {
            float y1 = barH * i;
            float y2 = y1 + barH;

            float off = totalW * prog;
            float x1 = (i % 2 == 0) ? -totalW + off : totalW - off;
            float x2 = x1 + totalW;

            drawGradientQuad(
                    x1, y1,
                    x2, y2,
                    dark.getRGB(),
                    light.getRGB()
            );
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawGradientQuad(float x1, float y1, float x2, float y2, int startColor, int endColor) {
        float a1 = (startColor >> 24 & 255) / 255.0F;
        float r1 = (startColor >> 16 & 255) / 255.0F;
        float g1 = (startColor >> 8 & 255) / 255.0F;
        float b1 = (startColor & 255) / 255.0F;

        float a2 = (endColor >> 24 & 255) / 255.0F;
        float r2 = (endColor >> 16 & 255) / 255.0F;
        float g2 = (endColor >> 8 & 255) / 255.0F;
        float b2 = (endColor & 255) / 255.0F;

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(r1, g1, b1, a1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y2);

        GL11.glColor4f(r2, g2, b2, a2);
        GL11.glVertex2f(x2, y2);
        GL11.glVertex2f(x2, y1);

        GL11.glEnd();
    }

    public static void updateProgress() {
        fakeProgress += 0.003f;
        if (fakeProgress > 1f) fakeProgress = 1f;
    }
}