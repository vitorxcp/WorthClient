package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ZoomHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static final float MIN_FOV = 1.0F;
    private static final float SCROLL_FACTOR = 2.0F;
    private static final float SMOOTH_FACTOR = 0.15F;
    private boolean active = false;
    private boolean wasKeyDown = false;
    private boolean running = false;
    private float originalFov = 70.0F;
    private float scrollOffset = 0.0F;
    private float originalSensitivity = 2.0F;
    private boolean originalSmoothCamera = false;

    @SubscribeEvent
    public void onMouseEvent(MouseEvent event) {
        if (active && event.dwheel != 0) {
            event.setCanceled(true);
            if (event.dwheel > 0) {
                scrollOffset -= SCROLL_FACTOR;
            } else {
                scrollOffset += SCROLL_FACTOR;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        boolean isKeyDown = Keybinds.ZoomM.isKeyDown();

        if (com.vitorxp.WorthClient.WorthClient.enableToggleZoom) {
            if (isKeyDown && !wasKeyDown) {
                active = !active;
                if (active) startZoom();
                else stopZoom();
            }
        } else {
            if (isKeyDown && !active) {
                active = true;
                startZoom();
            } else if (!isKeyDown && active) {
                active = false;
                stopZoom();
            }
        }
        wasKeyDown = isKeyDown;

        if (running) {
            float target;

            if (active) {
                float baseZoom = originalFov / 4.0F;
                target = baseZoom + scrollOffset;
                target = MathHelper.clamp_float(target, MIN_FOV, originalFov);
            } else {
                target = originalFov;
            }

            float currentFov = mc.gameSettings.fovSetting;
            if (Math.abs(currentFov - target) > 0.01F) {
                mc.gameSettings.fovSetting += (target - currentFov) * SMOOTH_FACTOR;
            } else {
                mc.gameSettings.fovSetting = target;
                if (!active) {
                    resetState();
                }
            }

            if (active) {
                float zoomFactor = mc.gameSettings.fovSetting / originalFov;
                mc.gameSettings.mouseSensitivity = originalSensitivity * zoomFactor;

                if (!mc.gameSettings.smoothCamera) {
                    mc.gameSettings.smoothCamera = true;
                }
            } else {
                mc.gameSettings.mouseSensitivity = originalSensitivity;
                mc.gameSettings.smoothCamera = originalSmoothCamera;
            }
        }
    }

    private void startZoom() {
        if (!running) {
            originalFov = mc.gameSettings.fovSetting;
            originalSensitivity = mc.gameSettings.mouseSensitivity;
            originalSmoothCamera = mc.gameSettings.smoothCamera;
        }
        running = true;
        scrollOffset = 0.0F;
        mc.gameSettings.smoothCamera = true;
    }

    private void stopZoom() {}

    private void resetState() {
        running = false;

        mc.gameSettings.fovSetting = originalFov;
        mc.gameSettings.mouseSensitivity = originalSensitivity;
        mc.gameSettings.smoothCamera = originalSmoothCamera;

        if (mc.renderGlobal != null && mc.thePlayer != null) {
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 64, (int) mc.thePlayer.posY - 64, (int) mc.thePlayer.posZ - 64,
                    (int) mc.thePlayer.posX + 64, (int) mc.thePlayer.posY + 64, (int) mc.thePlayer.posZ + 64
            );
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if (running && mc.currentScreen == null && !mc.gameSettings.showDebugInfo) {
            ScaledResolution sr = new ScaledResolution(mc);
            float fovRange = originalFov - MIN_FOV;
            float currentProgress = originalFov - mc.gameSettings.fovSetting;
            int percentage = Math.round((currentProgress / fovRange) * 100);
            percentage = MathHelper.clamp_int(percentage, 0, 100);
            if (percentage <= 1 && !active) return;
            String text = "Zoom: §a" + percentage + "%";
            int width = sr.getScaledWidth();
            int height = sr.getScaledHeight();
            int textWidth = mc.fontRendererObj.getStringWidth(text);
            int x = (width / 2) - (textWidth / 2);
            int y = height - 55;
            mc.fontRendererObj.drawStringWithShadow(text, x, y, 0xFFFFFFFF);
        }
    }
}