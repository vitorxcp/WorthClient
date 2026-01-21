package com.vitorxp.WorthClient.hud;

import com.vitorxp.WorthClient.config.KeystrokesSettings;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;

public class KeystrokesWasdHud extends HudElement {

    public KeystrokesWasdHud() { super("KeystrokesWASD", 10, 50); }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!KeystrokesSettings.enabled) return;

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y, 0);
        GlStateManager.scale(KeystrokesSettings.scale, KeystrokesSettings.scale, 1);

        int box = (int)KeystrokesSettings.boxSize;
        int gap = 2;

        if (KeystrokesSettings.showMovement) {
            KeystrokesDrawing.drawKey("W", box + gap, 0, Keyboard.isKeyDown(Keyboard.KEY_W));
            KeystrokesDrawing.drawKey("A", 0, box + gap, Keyboard.isKeyDown(Keyboard.KEY_A));
            KeystrokesDrawing.drawKey("S", box + gap, box + gap, Keyboard.isKeyDown(Keyboard.KEY_S));
            KeystrokesDrawing.drawKey("D", (box + gap) * 2, box + gap, Keyboard.isKeyDown(Keyboard.KEY_D));
        }

        if (KeystrokesSettings.showSpace) {
            int yStart = KeystrokesSettings.showMovement ? (box + gap) * 2 : 0;
            int width = (box * 3) + (gap * 2);
            KeystrokesDrawing.drawRect(0, yStart, width, box / 1.5f, Keyboard.isKeyDown(Keyboard.KEY_SPACE), "---");
        }

        GlStateManager.popMatrix();
    }

    @Override public int getWidth() { return (int)(((KeystrokesSettings.boxSize * 3) + 4) * KeystrokesSettings.scale); }
    @Override public int getHeight() { return (int)(((KeystrokesSettings.boxSize * 3) + 4) * KeystrokesSettings.scale); }
}