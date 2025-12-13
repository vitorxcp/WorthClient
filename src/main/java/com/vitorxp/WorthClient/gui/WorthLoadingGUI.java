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

    private static final int BG_COLOR = new Color(20, 20, 20).getRGB();
    private static final int BAR_BG = new Color(50, 50, 50).getRGB();
    private static final int BLUE_LUNAR = new Color(41, 187, 255).getRGB();

    public String text = "INICIANDO...";
    public float progress = 0f;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
    }

    private void initTextures() {
        if (LOGO_LOC != null) return;
        try {
            if (mc.getTextureManager() != null) {
                LOGO_LOC = GuiTextureLoader.load("textures/gui/logo_splash.png");
                BG_LOC = GuiTextureLoader.load("textures/gui/Background_3.png"); // Opcional
            }
        } catch (Exception e) {
            System.out.println("[WorthClient] Erro ao carregar texturas da Loading Screen");
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
            drawRect(0, 0, width, height, 0x88000000);
        }

        int centerX = width / 2;
        int centerY = height / 2;
        int logoSize = Math.min(150, height / 4);
        int barWidth = Math.min(300, width - 60);
        int barHeight = 6;
        int barY = height - (height / 4);

        drawLunarLogo(centerX, centerY - 30, logoSize);

        drawLunarBar(centerX, barY, barWidth, barHeight);
        drawStatusText(centerX, barY - 15);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawScreen(sr.getScaledWidth(), sr.getScaledHeight());
    }

    private void drawLunarLogo(int x, int y, int size) {
        GlStateManager.enableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        double time = System.currentTimeMillis() / 1000.0;
        float scale = 1.0f + (float) Math.sin(time * 2.5) * 0.04f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-size / 2.0f, -size / 2.0f, 0);

        if (LOGO_LOC != null) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            mc.getTextureManager().bindTexture(LOGO_LOC);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, size, size, size, size);
        } else {
            GlStateManager.disableTexture2D();
            drawRect(0, 0, size, size, 0xFFFF00FF);
            if (mc.fontRendererObj != null) {
                mc.fontRendererObj.drawStringWithShadow("NO IMAGE", 5, 5, 0xFFFFFF);
            }
            GlStateManager.enableTexture2D();
        }

        GlStateManager.popMatrix();
    }

    private void drawLunarBar(int centerX, int y, int width, int height) {
        int left = centerX - (width / 2);

        drawRect(left, y, left + width, y + height, BAR_BG);

        int fillWidth = (int) (width * progress);
        if (fillWidth > width) fillWidth = width;
        if (fillWidth > 0) {
            drawRect(left, y, left + fillWidth, y + height, BLUE_LUNAR);
        }
    }

    private void drawStatusText(int centerX, int y) {
        if (mc.fontRendererObj == null) return;
        GlStateManager.enableTexture2D();

        String status = text.toUpperCase();
        int strW = mc.fontRendererObj.getStringWidth(status);
        mc.fontRendererObj.drawStringWithShadow(status, centerX - (strW / 2), y, 0xAAAAAA);
    }
}