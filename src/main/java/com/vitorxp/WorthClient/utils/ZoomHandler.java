package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class ZoomHandler {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final float MIN_FOV = 1.0F;
    private static final float SCROLL_SENSITIVITY = 3.0F;

    private boolean isZooming = false;
    private float originalFov;
    private float targetFov;

    private float originalSensitivity;
    private boolean cinematicCameraState;

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (isZooming && event.dwheel != 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        boolean zoomKeyPressed = Keybinds.zoomKey.isKeyDown();

        if (zoomKeyPressed && !isZooming) {
            isZooming = true;
            originalFov = mc.gameSettings.fovSetting;
            targetFov = originalFov / 4;

            if (targetFov < MIN_FOV) {
                targetFov = MIN_FOV;
            }

            originalSensitivity = mc.gameSettings.mouseSensitivity;
            cinematicCameraState = mc.gameSettings.smoothCamera;
            mc.gameSettings.smoothCamera = true;

            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                    (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
            );
        }

        if (!zoomKeyPressed && isZooming) {
            isZooming = false;
            mc.gameSettings.fovSetting = originalFov;
            mc.gameSettings.mouseSensitivity = originalSensitivity;
            mc.gameSettings.smoothCamera = cinematicCameraState;

            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                    (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
            );
        }

        if (isZooming) {
            int wheel = Mouse.getDWheel();
            if (wheel != 0) {
                if (wheel < 0) {
                    targetFov += SCROLL_SENSITIVITY;
                } else {
                    targetFov -= SCROLL_SENSITIVITY;
                }
                targetFov = Math.max(MIN_FOV, Math.min(originalFov, targetFov));
            }

            mc.gameSettings.fovSetting = targetFov;

            float fovRatio = mc.gameSettings.fovSetting / originalFov;

            mc.gameSettings.mouseSensitivity = 0.3F;

            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                    (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
            );
        }
    }
}