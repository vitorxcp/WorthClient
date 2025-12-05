package com.vitorxp.WorthClient.gui;

import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.Minecraft;

public class VXLoadingScreenOverride extends LoadingScreenRenderer {

    private final Minecraft mc;
    private final VXLoadingScreenRenderer renderer;

    public VXLoadingScreenOverride(Minecraft mc, VXLoadingScreenRenderer renderer) {
        super(mc);
        this.mc = mc;
        this.renderer = renderer;
    }

    @Override
    public void displayLoadingString(String text) {
        renderer.render(text, 0f);
    }

    @Override
    public void setLoadingProgress(int progress) {
        float p = Math.max(0f, Math.min(1f, progress / 100f));
        renderer.render("Carregando WorthClient...", p);
    }

    @Override
    public void resetProgressAndMessage(String text) {
        renderer.render(text, 0f);
    }
}
