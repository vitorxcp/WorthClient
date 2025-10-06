package com.vitorxp.WorthClient.gui.utils;

import net.minecraft.client.gui.Gui;
import java.awt.Color;
import java.util.Random;

public class CosmicParticle {
    private static final Random random = new Random();
    private float x, y;
    private float radius;
    private float angle;
    private float speed;
    private float angularVelocity;
    private float size;
    private int life, maxLife;
    private Color color;

    public CosmicParticle(int centerX, int centerY) {
        this.radius = 0;
        this.angle = random.nextFloat() * 360.0f;
        this.speed = 0.5f + random.nextFloat() * 1.5f;
        this.angularVelocity = 0.5f + random.nextFloat() * 1.0f;
        this.maxLife = 100 + random.nextInt(100);
        this.life = this.maxLife;
        this.size = 0.5f + random.nextFloat() * 1.5f;
        this.color = new Color(255, 220, 150);
    }

    public void update(int centerX, int centerY) {
        if (this.life > 0) {
            this.life--;
            this.radius += this.speed;
            this.angle += this.angularVelocity;

            this.x = centerX + (float) (Math.cos(Math.toRadians(this.angle)) * this.radius);
            this.y = centerY + (float) (Math.sin(Math.toRadians(this.angle)) * this.radius);
        }
    }

    public void render() {
        if (this.life > 0) {
            float lifePercent = (float) this.life / (float) this.maxLife;
            float currentAlpha = AnimationUtil.easeOutCubic(lifePercent);
            float currentSize = this.size * lifePercent;

            int finalColor = new Color(
                    this.color.getRed() / 255f,
                    this.color.getGreen() / 255f,
                    this.color.getBlue() / 255f,
                    currentAlpha * 0.8f
            ).getRGB();

            Gui.drawRect((int)(this.x - currentSize / 2), (int)(this.y - currentSize / 2), (int)(this.x + currentSize / 2), (int)(this.y + currentSize / 2), finalColor);
        }
    }
}