package com.vitorxp.WorthClient.hud;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class FPSHUD extends HudElement {

    public static String messageDisplayHUDFPS = "§bFPS: §f0";

    public FPSHUD() {
        super("FPSHUD", 10, 50);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.fpsOverlay) return;

        String display = "§bFPS: §f" + Minecraft.getDebugFPS();
        messageDisplayHUDFPS = display;
        fontRenderer.drawStringWithShadow(display, this.x, this.y, 0xFFFFFF);
    }

    @Override
    public int getWidth() {
        return fontRenderer.getStringWidth(messageDisplayHUDFPS);
    }

    @Override
    public int getHeight() {
        return fontRenderer.FONT_HEIGHT;
    }
}