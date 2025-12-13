package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;

public class WorthLoadingGUI extends Gui {

    private final Minecraft mc;
    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_main.png");

    // Configurações de cores (Estilo Lunar)
    private static final int BG_COLOR = new Color(20, 20, 20).getRGB(); // Fundo quase preto
    private static final int BAR_BG_COLOR = new Color(40, 40, 40).getRGB(); // Fundo da barra
    private static final int GRADIENT_START = new Color(0, 205, 255).getRGB(); // Azul Ciano
    private static final int GRADIENT_END = new Color(0, 122, 255).getRGB();   // Azul Royal

    public String text = "Iniciando...";
    public float progress = 0f;

    public WorthLoadingGUI(Minecraft mc) {
        this.mc = mc;
    }

    public void update(String newText, float newProgress) {
        this.text = newText;
        this.progress = newProgress;
    }

    // Recebe largura e altura escaladas (ScaledResolution)
    public void drawScreen(int width, int height) {

        // 1. FUNDO SÓLIDO (Limpo)
        drawRect(0, 0, width, height, BG_COLOR);

        // 2. LOGO PULSANTE (BREATHING)
        drawAnimatedLogo(width, height);

        // 3. BARRA DE PROGRESSO (Estilo Lunar: Fina no rodapé)
        drawProgressBar(width, height);

        // 4. TEXTO E MEMÓRIA
        drawTextInfo(width, height);
    }

    private void drawAnimatedLogo(int w, int h) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Animação de Pulsar (Breathing)
        double time = System.currentTimeMillis() / 1000.0;
        float scale = 1.0f + (float) Math.sin(time * 2.0) * 0.05f; // Oscila 5%

        int imgW = 128; // Tamanho da logo
        int imgH = 128;

        // Centraliza e aplica zoom
        GlStateManager.translate(w / 2.0f, h / 2.0f - 20, 0); // -20 para subir um pouco
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-imgW / 2.0f, -imgH / 2.0f, 0);

        try {
            mc.getTextureManager().bindTexture(LOGO);
            drawModalRectWithCustomSizedTexture(0, 0, 0, 0, imgW, imgH, imgW, imgH);
        } catch (Exception e) {
            // Se não tiver logo, desenha texto placeholder
            GlStateManager.popMatrix(); // Reseta matrix antes de desenhar texto
            drawCenteredString(mc.fontRendererObj, "WORTH CLIENT", w/2, h/2 - 20, -1);
            return;
        }
        GlStateManager.popMatrix();
    }

    private void drawProgressBar(int w, int h) {
        int barHeight = 6; // Barra fina
        int barY = h - 40; // Um pouco acima do fim da tela
        int barWidth = w - 100; // Margem de 50px de cada lado
        int barX = 50;

        // Fundo da barra (Cinza Escuro)
        drawRect(barX, barY, barX + barWidth, barY + barHeight, BAR_BG_COLOR);

        // Preenchimento com Degradê
        int fillWidth = (int) (barWidth * progress);
        if (fillWidth > 0) {
            drawGradientRect(barX, barY, barX + fillWidth, barY + barHeight, GRADIENT_START, GRADIENT_END);
        }
    }

    private void drawTextInfo(int w, int h) {
        if (mc.fontRendererObj == null) return;

        int barY = h - 40;
        int barX = 50;

        // Texto "Iniciando..." acima da barra
        mc.fontRendererObj.drawStringWithShadow(text, barX, barY - 12, 0xFFFFFF);

        // Porcentagem na direita
        String pct = (int)(progress * 100) + "%";
        int pctW = mc.fontRendererObj.getStringWidth(pct);
        mc.fontRendererObj.drawStringWithShadow(pct, w - 50 - pctW, barY - 12, 0xAAAAAA);

        // Info de Memória (Pequeno no canto inferior direito)
        long maxMem = Runtime.getRuntime().maxMemory();
        long totalMem = Runtime.getRuntime().totalMemory();
        long freeMem = Runtime.getRuntime().freeMemory();
        long usedMem = totalMem - freeMem;

        String ram = "RAM: " + (usedMem / 1024L / 1024L) + "MB / " + (maxMem / 1024L / 1024L) + "MB";
        int ramW = mc.fontRendererObj.getStringWidth(ram);

        // Desenha bem no cantinho
        mc.fontRendererObj.drawStringWithShadow(ram, w - ramW - 5, h - 12, 0x555555);
    }
}