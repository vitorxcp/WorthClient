package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class PerspectiveMod {

    private final Minecraft mc;

    public PerspectiveMod() {
        this.mc = Minecraft.getMinecraft();
    }

    private boolean enabled = false;
    private boolean wasKeyDown = false;

    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    private int previousThirdPersonView = 0;

    private void enable() {
        if (enabled) return;
        enabled = true;

        previousThirdPersonView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 2;

        if (mc.thePlayer != null) {
            cameraYaw = mc.thePlayer.rotationYaw;
            cameraPitch = mc.thePlayer.rotationPitch;
        }

        if (mc.renderGlobal != null) {
            assert mc.thePlayer != null;
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                    (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
            );
        }
    }

    private void disable() {
        if (!enabled) return;
        enabled = false;
        mc.gameSettings.thirdPersonView = previousThirdPersonView;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        boolean toggleMode = com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle;

        if (mc.thePlayer == null || mc.theWorld == null) return;
        boolean keyDown = Keybinds.perspectiveM.isKeyDown();

        if (toggleMode) {
            if (keyDown && !wasKeyDown) {
                if (enabled) disable(); else enable();
            }
        } else {
            if (keyDown && !enabled) enable();
            if (!keyDown && enabled) disable();
        }

        wasKeyDown = keyDown;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (!enabled) return;

        if (!mc.inGameHasFocus) return;

        handleMouseMovement();
    }

    private void handleMouseMovement() {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float mul = f * f * f * 8.0F;

        float dx = Mouse.getDX() * mul * 0.15F;
        float dy = Mouse.getDY() * mul * 0.15F;

        cameraYaw += dx;
        cameraPitch -= dy;

        if (cameraPitch > 90F) cameraPitch = 90F;
        if (cameraPitch < -90F) cameraPitch = -90F;

        if (cameraYaw <= -180F) cameraYaw += 360F;
        if (cameraYaw > 180F) cameraYaw -= 360F;

        mc.renderGlobal.markBlockRangeForRenderUpdate(
                (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
        );
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (!enabled) return;

        try {
            mc.renderGlobal.markBlockRangeForRenderUpdate(
                    (int) mc.thePlayer.posX - 512, 0, (int) mc.thePlayer.posZ - 512,
                    (int) mc.thePlayer.posX + 512, 256, (int) mc.thePlayer.posZ + 512
            );
            event.yaw = cameraYaw;
            event.pitch = cameraPitch;
        } catch (NoSuchFieldError | NoSuchMethodError ex) {
            try {
                event.getClass().getMethod("setYaw", float.class).invoke(event, cameraYaw);
                event.getClass().getMethod("setPitch", float.class).invoke(event, cameraYaw);
            } catch (Exception ignored) {
            }
        }
    }
}