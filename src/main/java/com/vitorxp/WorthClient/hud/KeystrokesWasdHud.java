// Renomeie o arquivo para KeystrokesWasdHud.java
package com.vitorxp.WorthClient.hud;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.input.Keyboard;

public class KeystrokesWasdHud extends HudElement {

    private final int boxSize = 20;
    private final int padding = 2;
    private final int spacing = boxSize + padding;

    public KeystrokesWasdHud() {
        super("KeystrokesWASD", 10, 50);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.keystrokesOverlay) return;

        KeystrokesDrawing.drawKey("W", this.x + spacing, this.y, Keyboard.isKeyDown(Keyboard.KEY_W), boxSize, boxSize);
        KeystrokesDrawing.drawKey("A", this.x, this.y + spacing, Keyboard.isKeyDown(Keyboard.KEY_A), boxSize, boxSize);
        KeystrokesDrawing.drawKey("S", this.x + spacing, this.y + spacing, Keyboard.isKeyDown(Keyboard.KEY_S), boxSize, boxSize);
        KeystrokesDrawing.drawKey("D", this.x + spacing * 2, this.y + spacing, Keyboard.isKeyDown(Keyboard.KEY_D), boxSize, boxSize);

        KeystrokesDrawing.drawKey("§n__", this.x, this.y + spacing * 2, Keyboard.isKeyDown(Keyboard.KEY_SPACE), boxSize*3 + padding*2, boxSize);
    }

    @Override
    public int getWidth() {
        return boxSize * 3 + padding * 2;
    }

    @Override
    public int getHeight() {
        return boxSize * 3 + padding * 2;
    }
}