package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorthLoadingGUI extends GuiScreen {

    private static final int BG_COLOR = new Color(12, 12, 12).getRGB();
    private static final int BAR_BG_COLOR = new Color(30, 30, 30).getRGB();
    private static final int TEXT_COLOR = new Color(220, 220, 220).getRGB();
    private static final int RAM_TEXT_COLOR = new Color(100, 100, 100).getRGB();
    private static ResourceLocation LOGO_LOC;
    private static ResourceLocation CLIENT_LOGO_LOC;
    private static ResourceLocation BG_LOC;
    private String currentStep = "Iniciando...";
    private float targetProgress = 0.0f;
    private static float visualProgress = 0.0f;
    private static String currentTip = "Carregando...";
    private static long lastTipTime = 0;
    private static final long TIP_INTERVAL = 4000;
    private static final List<String> TIPS = Arrays.asList(
            "Dica: Pressione RSHIFT para abrir o Menu do Cliente.",
            "Dica: Você pode mover e personalizar a HUD pressionando RSHIFT.",
            "Dica: Segure a tecla 'C' para usar o Zoom.",
            "Dica: O menu de Mods permite ativar ou desativar recursos instantaneamente.",
            "Dica: O 'AutoLogin' digita sua senha automaticamente ao entrar no servidor.",
            "Dica: Use o 'Perspective Mod' para olhar ao redor sem girar o corpo.",
            "Dica: Configure o 'AutoText' para enviar comandos ou frases rápidas.",
            "Curiosidade: O WorthClient suporta Skins 3D e Capas Animadas.",
            "Curiosidade: O Discord RPC mostra para seus amigos onde você está jogando.",
            "Dica: Otimizações de FPS e memória são ativadas automaticamente.",
            "Dica: O cliente agrupa mensagens repetidas de venda e leilão no chat.",
            "Curiosidade: O WorthClient bloqueia partículas inúteis para aumentar o FPS.",
            "Dica: Configure o filtro de chat para esconder mensagens de spam.",
            "Dica: Entre no nosso Discord para sugerir novas funções!",
            "Curiosidade: Este cliente foi desenvolvido com foco total em performance.",
            "Dica: Pressione RSHIFT para abrir o Menu de Mods.",
            "Você sabia? WorthClient aumenta seu FPS em até 40%.",
            "Dica: Personalize sua HUD arrastando os elementos.",
            "Dica: Use o Zoom pressionando a tecla C.",
            "Status: Otimizando texturas em tempo real...",
            "Dica: Entre no Discord para sugerir novidades!",
            "Dica: O AutoLogin protege sua conta automaticamente.",
            "Curiosidade: Use o Perspective Mod para olhar ao redor."
    );

    public WorthLoadingGUI() {
        this.mc = Minecraft.getMinecraft();
        if (lastTipTime == 0) {
            updateTip();
        }
    }

    private void updateTip() {
        if (TIPS != null && !TIPS.isEmpty()) {
            currentTip = TIPS.get(new Random().nextInt(TIPS.size()));
        }
        lastTipTime = System.currentTimeMillis();
    }

    private void initTextures() {
        if (mc.getTextureManager() == null) return;

        if (LOGO_LOC != null && CLIENT_LOGO_LOC != null && BG_LOC != null) return;

        try {
            if (LOGO_LOC == null)
                LOGO_LOC = loadTextureSafe("splash_logo", "/assets/worthclient/textures/gui/logo_splash.png");
            if (BG_LOC == null)
                BG_LOC = loadTextureSafe("splash_bg", "/assets/worthclient/textures/gui/Background_3.png");
            if (CLIENT_LOGO_LOC == null)
                CLIENT_LOGO_LOC = loadTextureSafe("client_logo_bottom", "/assets/worthclient/textures/gui/logo_client.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResourceLocation loadTextureSafe(String name, String path) throws Exception {
        if (mc.getTextureManager() == null) return null;

        InputStream is = getClass().getResourceAsStream(path);
        if (is == null) return null;

        BufferedImage img = ImageIO.read(is);
        DynamicTexture tex = new DynamicTexture(img);
        return mc.getTextureManager().getDynamicTextureLocation(name, tex);
    }

    private float lerp(float start, float end, float step) {
        return start + (end - start) * step;
    }

    public void drawProgress(String step, float progress) {
        this.currentStep = step;
        this.targetProgress = progress;

        if (System.currentTimeMillis() - lastTipTime > TIP_INTERVAL) {
            updateTip();
        }

        float diff = targetProgress - visualProgress;
        float speed = (diff > 0.2f) ? 0.2f : 0.08f;
        visualProgress = lerp(visualProgress, targetProgress, speed);

        if (targetProgress >= 0.99f && visualProgress >= 0.95f) visualProgress = 1.0f;
        if (visualProgress > 1.0f) visualProgress = 1.0f;
        if (visualProgress < 0.0f) visualProgress = 0.0f;
        if (mc.displayWidth <= 0 || mc.displayHeight <= 0) return;

        int w = mc.displayWidth;
        int h = mc.displayHeight;
        ScaledResolution sr = new ScaledResolution(mc);
        int scaleFactor = sr.getScaleFactor();
        int width = w / scaleFactor;
        int height = h / scaleFactor;

        GlStateManager.viewport(0, 0, w, h);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, width, height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        drawRect(0, 0, width, height, BG_COLOR);
        initTextures();

        if (BG_LOC != null) {
            GlStateManager.color(0.7f, 0.7f, 0.7f, 1.0f);
            mc.getTextureManager().bindTexture(BG_LOC);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
            drawGradientRect(0, 0, width, height / 3, 0xAA000000, 0x00000000);
            drawGradientRect(0, height - (height / 3), width, height, 0x00000000, 0xCC000000);
        }

        int centerX = width / 2;
        int centerY = height / 2;

        if (LOGO_LOC != null) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            mc.getTextureManager().bindTexture(LOGO_LOC);

            double time = System.currentTimeMillis() / 800.0;
            float pulse = 1.0f + (float) Math.sin(time) * 0.03f;
            float floatY = (float) Math.cos(time) * 3.0f;

            int logoSize = 110;

            GlStateManager.pushMatrix();
            GlStateManager.translate(centerX, centerY - 50 + floatY, 0);
            GlStateManager.scale(pulse, pulse, 1.0f);
            GlStateManager.translate(-logoSize / 2.0f, -logoSize / 2.0f, 0);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, logoSize, logoSize, logoSize, logoSize);
            GlStateManager.popMatrix();
        }

        int barWidth = 240;
        int barHeight = 6;
        int barX = centerX - (barWidth / 2);
        int barY = centerY + 35;

        drawRect(barX, barY, barX + barWidth, barY + barHeight, BAR_BG_COLOR);

        int fill = (int) (barWidth * visualProgress);
        if (fill > 0) {
            drawGradientRectHorizontal(barX, barY, barX + fill, barY + barHeight, 0xFFFFAA00, 0xFFFF5500);
            drawRect(barX + fill - 2, barY, barX + fill, barY + barHeight, 0xFFFFFFFF);
        }

        if (mc.fontRendererObj != null) {
            String percentText = (int) (visualProgress * 100) + "%";
            mc.fontRendererObj.drawStringWithShadow(percentText, centerX - (mc.fontRendererObj.getStringWidth(percentText) / 2), barY - 12, TEXT_COLOR);
            mc.fontRendererObj.drawStringWithShadow(this.currentStep, centerX - (mc.fontRendererObj.getStringWidth(this.currentStep) / 2), barY + 10, 0xAAAAAA);
        }

        if (CLIENT_LOGO_LOC != null) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            mc.getTextureManager().bindTexture(CLIENT_LOGO_LOC);

            int bottomLogoH = 35;
            int bottomLogoW = (int) (bottomLogoH * (1640.0f / 664.0f));
            int bottomLogoX = centerX - (bottomLogoW / 2);
            int bottomLogoY = height - 60;

            GlStateManager.enableBlend();
            drawModalRectWithCustomSizedTexture(bottomLogoX, bottomLogoY, 0, 0, bottomLogoW, bottomLogoH, bottomLogoW, bottomLogoH);
        }

        if (mc.fontRendererObj != null) {
            mc.fontRendererObj.drawStringWithShadow(currentTip, centerX - (mc.fontRendererObj.getStringWidth(currentTip) / 2), height - 20, 0xFFFFCC);

            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = totalMem - freeMem;

            String ramText = String.format("MEM: %dMB / %dMB", usedMem / 1024 / 1024, maxMem / 1024 / 1024);
            int ramW = mc.fontRendererObj.getStringWidth(ramText);
            mc.fontRendererObj.drawStringWithShadow(ramText, width - ramW - 5, height - 12, RAM_TEXT_COLOR);
            mc.fontRendererObj.drawStringWithShadow("(BETA)", 5, height - 12, 0x555555);
        }

        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    protected void drawGradientRectHorizontal(int left, int top, int right, int bottom, int startColor, int endColor) {
        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double) right, (double) top, 0.0D).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos((double) left, (double) top, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double) left, (double) bottom, 0.0D).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos((double) right, (double) bottom, 0.0D).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}