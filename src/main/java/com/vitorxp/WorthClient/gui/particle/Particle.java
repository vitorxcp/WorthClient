package com.vitorxp.WorthClient.gui.particle;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.Random;

public class Particle {
    private static final Random random = new Random();
    private float x, y;
    private float velocityY;
    private float size;
    private float alpha;

    public Particle(int screenWidth, int screenHeight) {
        this.x = random.nextFloat() * screenWidth;
        this.y = random.nextFloat() * screenHeight;
        this.reset();
    }

    private void reset() {
        this.velocityY = -(0.1F + random.nextFloat() * 0.3F);
        this.size = 1.0F + random.nextFloat() * 1.5F;
        this.alpha = 0.0F; // Começa invisível
    }

    public void update(int screenWidth, int screenHeight) {
        if (alpha < 1.0F) {
            alpha += 0.02F;
        }

        this.y += this.velocityY;

        if (this.y < 0) {
            this.y = screenHeight;
            this.x = random.nextFloat() * screenWidth;
            this.reset();
        }
    }

    public void render() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Color particleColor = new Color(200, 180, 255, (int)(this.alpha * 80));
        Gui.drawRect((int)this.x, (int)this.y, (int)(this.x + this.size), (int)(this.y + this.size), particleColor.getRGB());

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}