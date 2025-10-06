package com.vitorxp.WorthClient.hud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Mouse;

public class KeystrokesRmbHud extends HudElement {

    public KeystrokesRmbHud() {
        super("KeystrokesRMB", 80, 120);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.keystrokesOverlay) return;

        int cps = com.vitorxp.WorthClient.WorthClient.keystrokesManager.getCpsRight();
        KeystrokesDrawing.drawCpsKey("RMB", cps, this.x, this.y, Mouse.isButtonDown(1), getWidth(), getHeight());
    }

    @Override
    public int getWidth() { return 60; }

    @Override
    public int getHeight() { return 20; }
}