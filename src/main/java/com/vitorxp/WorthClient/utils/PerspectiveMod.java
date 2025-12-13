package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

public class PerspectiveMod {

    private final Minecraft mc;
    public static boolean perspectiveToggled = false;
    public static float cameraYaw = 0f;
    public static float cameraPitch = 0f;
    private boolean wasKeyDown = false;
    private int previousThirdPersonView = 0;

    public PerspectiveMod() {
        this.mc = Minecraft.getMinecraft();
    }

    public void enable() {
        if (perspectiveToggled) return;
        perspectiveToggled = true;

        previousThirdPersonView = mc.gameSettings.thirdPersonView;

        mc.gameSettings.thirdPersonView = 1;

        if (mc.thePlayer != null) {
            if (WorthClient.PerspectiveStartFront) {
                cameraYaw = mc.thePlayer.rotationYaw + 180f;
                cameraPitch = mc.thePlayer.rotationPitch;
            } else {
                cameraYaw = mc.thePlayer.rotationYaw;
                cameraPitch = mc.thePlayer.rotationPitch;
            }
        }
    }

    public void disable() {
        if (!perspectiveToggled) return;
        perspectiveToggled = false;

        mc.gameSettings.thirdPersonView = previousThirdPersonView;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean toggleMode = com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle;
        boolean keyDown = Keybinds.perspectiveM.isKeyDown();

        if (toggleMode) {
            if (keyDown && !wasKeyDown) {
                if (perspectiveToggled) disable(); else enable();
            }
        } else {
            if (keyDown && !perspectiveToggled) enable();
            if (!keyDown && perspectiveToggled) disable();
        }
        wasKeyDown = keyDown;

        if (perspectiveToggled && mc.gameSettings.thirdPersonView != 1) {
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (perspectiveToggled && mc.inGameHasFocus && Display.isActive()) {
            handleMouseMovement();
        }
    }

    public void handleMouseMovement() {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float mul = f * f * f * 8.0F;

        float dx = Mouse.getDX() * mul * 0.15F;
        float dy = Mouse.getDY() * mul * 0.15F;

        cameraYaw += dx;

        if (mc.gameSettings.invertMouse) {
            cameraPitch += dy;
        } else {
            cameraPitch -= dy;
        }

        if (cameraPitch > 90F) cameraPitch = 90F;
        if (cameraPitch < -90F) cameraPitch = -90F;

        if (cameraYaw <= -180F) cameraYaw += 360F;
        if (cameraYaw > 180F) cameraYaw -= 360F;
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (perspectiveToggled) {
            event.yaw = cameraYaw;
            event.pitch = cameraPitch;
        }
    }
}