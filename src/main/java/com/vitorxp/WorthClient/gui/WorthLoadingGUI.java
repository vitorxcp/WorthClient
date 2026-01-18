package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.utils.LoadingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorthLoadingGUI extends GuiScreen {

    private static final int BG_COLOR = new Color(10, 10, 10).getRGB();
    private static final int BAR_BG_COLOR = new Color(55, 50, 25).getRGB();
    private static final int BAR_FILL_COLOR = new Color(255, 170, 0).getRGB();
    private static final int TEXT_COLOR = new Color(255, 255, 240).getRGB();
    private static final int SUB_TEXT_COLOR = new Color(150, 150, 150).getRGB();

    private ResourceLocation LOGO_LOC;
    private ResourceLocation BG_LOC;
    private ResourceLocation CLIENT_LOGO_LOC;

    public String text = "Inicializando...";
    public float currentProgress = 0f;
    public float targetProgress = 0f;

    private String currentTip;

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
            "Curiosidade: Este cliente foi desenvolvido com foco total em performance."
    );

    public WorthLoadingGUI() {
        this.mc = Minecraft.getMinecraft();
        if (TIPS != null && !TIPS.isEmpty()) {
            this.currentTip = TIPS.get(new Random().nextInt(TIPS.size()));
        } else {
            this.currentTip = "WorthClient - PvP Otimizado";
        }
    }

    private void initTextures() {
        if (LOGO_LOC != null) return;
        try {
            if (mc.getTextureManager() != null) {
                LOGO_LOC = new ResourceLocation("worthclient", "textures/gui/logo_splash.png");
                BG_LOC = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
                CLIENT_LOGO_LOC = new ResourceLocation("worthclient", "textures/gui/logo_client.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(String newText, float newProgress) {
        if (newText != null) this.text = newText;
        if (newProgress >= 0) this.targetProgress = newProgress;
    }

    private float interpolate(float current, float target, float speed) {
        return current + (target - current) * speed;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        drawContent(sr.getScaledWidth(), sr.getScaledHeight());
    }

    public void drawManual(int width, int height) {
        int scale = 2;
        drawContent(width / scale, height / scale);
    }

    public void drawContent(int width, int height) {
        this.text = LoadingUtils.getCurrentText();
        this.targetProgress = LoadingUtils.getCurrentProgress();

        if (this.text.equalsIgnoreCase("Pronto!") || this.targetProgress >= 1.0f) {
            this.currentProgress = 1.0f;
            this.targetProgress = 1.0f;
        } else {
            this.currentProgress = interpolate(this.currentProgress, this.targetProgress, 0.1f);
        }
        if (this.currentProgress > 1.0f) this.currentProgress = 1.0f;

        initTextures();
        drawRect(0, 0, width, height, BG_COLOR);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        if (BG_LOC != null) {
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            try {
                mc.getTextureManager().bindTexture(BG_LOC);
                drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);
                drawRect(0, 0, width, height, 0xCC000000);
            } catch (Exception ignored) {}
        }

        int centerX = width / 2;
        int logoSize = 100;
        int barWidth = 220;
        int barHeight = 10;

        int totalContentHeight = logoSize + 15 + 10 + 20;
        int startY = (height - totalContentHeight) / 2;

        int logoY = startY + (logoSize / 2);
        int textY = startY + logoSize + 10;
        int barY = textY + 15;

        drawLogo(centerX, logoY, logoSize);
        drawStatusText(centerX, textY);
        drawRoundedGoldBar(centerX, barY, barWidth, barHeight);

        drawBottomInfo(width, height);
    }

    private void drawLogo(int x, int y, int size) {
        if (LOGO_LOC == null) return;
        try {
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            float pulse = 1.0f + (float) Math.sin(System.currentTimeMillis() / 600.0) * 0.02f;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(pulse, pulse, 1.0f);
            GlStateManager.translate(-size / 2.0f, -size / 2.0f, 0);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            mc.getTextureManager().bindTexture(LOGO_LOC);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, size, size, size, size);
            GlStateManager.popMatrix();
        } catch (Exception ignored) {}
    }

    private void drawRoundedGoldBar(int centerX, int y, int width, int height) {
        int left = centerX - (width / 2);
        int right = left + width;
        int bottom = y + height;

        drawRect(left + 2, y, right - 2, bottom, BAR_BG_COLOR);
        drawRect(left, y + 1, left + 2, bottom - 1, BAR_BG_COLOR);
        drawRect(right - 2, y + 1, right, bottom - 1, BAR_BG_COLOR);

        int fillWidth = (int) (width * currentProgress);
        if (fillWidth > width) fillWidth = width;
        if (fillWidth < 0) fillWidth = 0;

        if (fillWidth > 0) {
            int fillRight = left + fillWidth;
            if (fillRight - 2 > left + 2)
                drawRect(left + 2, y, fillRight - 2, bottom, BAR_FILL_COLOR);
            drawRect(left, y + 1, left + 2, bottom - 1, BAR_FILL_COLOR);
            if (fillWidth >= width - 2) {
                drawRect(fillRight - 2, y + 1, fillRight, bottom - 1, BAR_FILL_COLOR);
            } else if (fillWidth > 4) {
                drawRect(fillRight - 2, y + 1, fillRight, bottom - 1, BAR_FILL_COLOR);
            }
        }
    }

    private void drawStatusText(int centerX, int y) {
        if (mc.fontRendererObj == null) return;
        GlStateManager.enableTexture2D();

        String fullText;
        if (currentProgress >= 0.99f || text.toLowerCase().contains("pronto")) {
            fullText = "Finalizando...";
        } else {
            String percent = String.format("%d%%", (int)(currentProgress * 100));
            fullText = text + " " + percent;
        }

        int strW = mc.fontRendererObj.getStringWidth(fullText);
        mc.fontRendererObj.drawStringWithShadow(fullText, centerX - (strW / 2), y, TEXT_COLOR);
    }

    private void drawBottomInfo(int width, int height) {
        if (mc.fontRendererObj == null) return;

        if (CLIENT_LOGO_LOC != null) {
            GlStateManager.enableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            try {
                mc.getTextureManager().bindTexture(CLIENT_LOGO_LOC);

                int logoH = 25;
                int logoW = (int) (logoH * (1640.0f / 664.0f));

                int logoX = 5;
                int logoY = height - logoH - 5;

                drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoW, logoH, logoW, logoH);
            } catch (Exception ignored) {}
        } else {
            mc.fontRendererObj.drawStringWithShadow("WorthClient", 5, height - 12, 0xAAAAAA);
        }

        int tipWidth = mc.fontRendererObj.getStringWidth(currentTip);
        mc.fontRendererObj.drawStringWithShadow(currentTip, (width / 2) - (tipWidth / 2), height - 35, 0xFFCC00);

        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;

        String memText = String.format("Memória: %dMB / %dMB", usedMem / 1024 / 1024, maxMem / 1024 / 1024);
        int memWidth = mc.fontRendererObj.getStringWidth(memText);

        mc.fontRendererObj.drawStringWithShadow(memText, width - memWidth - 5, height - 12, SUB_TEXT_COLOR);
    }
}