package com.vitorxp.WorthClient.hud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Mouse;

public class KeystrokesLmbHud extends HudElement {

    public KeystrokesLmbHud() {
        super("KeystrokesLMB", 10, 120);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.keystrokesOverlay) return;

        int cps = com.vitorxp.WorthClient.WorthClient.keystrokesManager.getCpsLeft();
        KeystrokesDrawing.drawCpsKey("LMB", cps, this.x, this.y, Mouse.isButtonDown(0), getWidth(), getHeight());
    }

    @Override
    public int getWidth() { return 60; }

    @Override
    public int getHeight() { return 20; }
}