package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Mouse;

public class KeystrokesRmbHud extends HudElement {

    public KeystrokesRmbHud() {
        super("KeystrokesRMB", 80, 120);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!SkyBlockMod.keystrokesOverlay) return;

        int cps = SkyBlockMod.keystrokesManager.getCpsRight();
        KeystrokesDrawing.drawCpsKey("RMB", cps, this.x, this.y, Mouse.isButtonDown(1), getWidth(), getHeight());
    }

    @Override
    public int getWidth() { return 60; }

    @Override
    public int getHeight() { return 20; }
}