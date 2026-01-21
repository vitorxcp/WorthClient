package com.vitorxp.WorthClient.hud;

import com.vitorxp.WorthClient.config.KeystrokesSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;

public class KeystrokesLmbHud extends HudElement {

    public KeystrokesLmbHud() { super("KeystrokesLMB", 10, 120); }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!KeystrokesSettings.enabled || !KeystrokesSettings.showClicks) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y, 0);
        GlStateManager.scale(KeystrokesSettings.scale, KeystrokesSettings.scale, 1);

        int w = (int)(KeystrokesSettings.boxSize * 1.5 + 1);
        int h = (int)KeystrokesSettings.boxSize;
        int cps = com.vitorxp.WorthClient.WorthClient.keystrokesManager.getCpsLeft();

        KeystrokesDrawing.drawCps("LMB", cps, 0, 0, Mouse.isButtonDown(0), w, h);
        GlStateManager.popMatrix();
    }

    @Override public int getWidth() { return (int)((KeystrokesSettings.boxSize * 1.5 + 1) * KeystrokesSettings.scale); }
    @Override public int getHeight() { return (int)(KeystrokesSettings.boxSize * KeystrokesSettings.scale); }
}