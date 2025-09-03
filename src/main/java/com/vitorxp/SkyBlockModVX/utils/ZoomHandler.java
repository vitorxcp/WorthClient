package com.vitorxp.SkyBlockModVX.utils;

import com.vitorxp.SkyBlockModVX.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class ZoomHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Configurações
    private float targetZoom = 1.0f;      // zoom atual que queremos aplicar
    private float currentZoom = 1.0f;     // zoom atual interpolado
    private float minZoom = 0.1f;         // zoom máximo
    private float maxZoom = 1.0f;         // sem zoom
    private float zoomStep = 0.05f;       // ajuste por scroll
    private float smoothSpeed = 0.1f;     // velocidade de interpolação

    private boolean zooming = false;
    private float baseFov = 0f;

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (Keybinds.zoomKey.isPressed() && !zooming) {
            zooming = true;
            baseFov = mc.gameSettings.fovSetting;
            targetZoom = 0.5f; // zoom inicial ao apertar
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Scroll para aumentar/diminuir zoom
        if (zooming) {
            int dWheel = Mouse.getDWheel();
            if (dWheel != 0) {
                if (dWheel < 0) targetZoom += zoomStep;
                if (dWheel > 0) targetZoom -= zoomStep;

                if (targetZoom < minZoom) targetZoom = minZoom;
                if (targetZoom > maxZoom) targetZoom = maxZoom;
            }
        }

        // Tecla pressionada
        if (Keybinds.zoomKey.isKeyDown()) {
            zooming = true;
            // Interpolação suave
            currentZoom += (targetZoom - currentZoom) * smoothSpeed;
            mc.gameSettings.fovSetting = baseFov * currentZoom;
        } else {
            if (zooming) {
                // Volta suavemente ao FOV normal
                currentZoom += (1.0f - currentZoom) * smoothSpeed;
                mc.gameSettings.fovSetting = baseFov * currentZoom;

                // Quando estiver quase no FOV original, reset
                if (Math.abs(currentZoom - 1.0f) < 0.01f) {
                    mc.gameSettings.fovSetting = baseFov;
                    zooming = false;
                    currentZoom = 1.0f;
                    targetZoom = 1.0f;
                }
            }
        }
    }
}
