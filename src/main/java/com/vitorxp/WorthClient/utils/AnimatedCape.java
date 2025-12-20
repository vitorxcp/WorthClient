package com.vitorxp.WorthClient.utils;

import net.minecraft.util.ResourceLocation;

public class AnimatedCape {
    private final ResourceLocation textureLocation;
    private final int totalFrames;
    private final int delay;

    private int currentFrame;
    private long lastTickTime;

    public AnimatedCape(ResourceLocation textureLocation, int totalFrames, int delay) {
        this.textureLocation = textureLocation;
        this.totalFrames = totalFrames;
        this.delay = delay;
        this.currentFrame = 0;
        this.lastTickTime = System.currentTimeMillis();
    }

    public void update() {
        if (totalFrames <= 1) return;

        long now = System.currentTimeMillis();
        if (now - lastTickTime >= delay) {
            currentFrame = (currentFrame + 1) % totalFrames;
            lastTickTime = now;
        }
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    public ResourceLocation getTexture() {
        return textureLocation;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }
}