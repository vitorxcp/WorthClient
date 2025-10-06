package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import com.vitorxp.WorthClient.gui.utils.CosmicParticle;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class GuiCosmicSplashScreen extends GuiScreen {

    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "textures/gui/logo_splash.png");
    private List<CosmicParticle> particles = new ArrayList<>();
    private long startTime;
    private final int totalDuration = 5000;

    @Override
    public void initGui() {
        this.startTime = System.currentTimeMillis();
        if (this.particles.isEmpty()) {
            for (int i = 0; i < 400; i++) {
                this.particles.add(new CosmicParticle(this.width / 2, this.height / 2));
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long elapsedTime = System.currentTimeMillis() - this.startTime;
        float progress = (float)elapsedTime / totalDuration;

        if (elapsedTime > totalDuration) {
            this.mc.displayGuiScreen(new GuiClientMainMenu());
            return;
        }

        Color startColor = Color.BLACK;
        Color endColor = new Color(74, 50, 13);
        float bgProgress = Math.min(1.0f, (float)elapsedTime / 2500.0f);

        int r = (int) AnimationUtil.lerp(startColor.getRed(), endColor.getRed(), bgProgress);
        int g = (int) AnimationUtil.lerp(startColor.getGreen(), endColor.getGreen(), bgProgress);
        int b = (int) AnimationUtil.lerp(startColor.getBlue(), endColor.getBlue(), bgProgress);
        drawRect(0, 0, this.width, this.height, new Color(r,g,b).getRGB());

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        for (CosmicParticle p : this.particles) {
            p.update(this.width / 2, this.height / 2);
            p.render();
        }
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        float logoRevealProgress = 0f;
        if (elapsedTime > 1500) {
            logoRevealProgress = Math.min(1.0f, (elapsedTime - 1500) / 2500.0f);
        }
        float logoAlpha = AnimationUtil.easeOutCubic(logoRevealProgress);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, logoAlpha);

        int logoWidth = 128;
        int logoHeight = 128;

        this.mc.getTextureManager().bindTexture(LOGO);
        Gui.drawModalRectWithCustomSizedTexture(
                this.width / 2 - logoWidth / 2, this.height / 2 - logoHeight / 2,
                0, 0, logoWidth, logoHeight, logoWidth, logoHeight
        );
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (elapsedTime > 4000) {
            float fadeOutProgress = (elapsedTime - 4000) / 1000.0f;
            drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, fadeOutProgress).getRGB());
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}