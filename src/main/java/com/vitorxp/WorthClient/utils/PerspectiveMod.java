package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PerspectiveMod {

    private final Minecraft mc;
    public static boolean perspectiveToggled = false;
    public static float cameraYaw = 0f;
    public static float cameraPitch = 0f;
    public static float prevCameraYaw = 0f;
    public static float prevCameraPitch = 0f;
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
            cameraPitch = mc.thePlayer.rotationPitch;
            prevCameraPitch = cameraPitch;

            float yawOffset = WorthClient.PerspectiveStartFront ? 180f : 0f;
            cameraYaw = mc.thePlayer.rotationYaw + yawOffset;
            prevCameraYaw = cameraYaw;
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

        boolean toggleMode = WorthClient.PerspectiveModToggle;
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

        if (perspectiveToggled) {
            prevCameraYaw = cameraYaw;
            prevCameraPitch = cameraPitch;

            if (mc.gameSettings.thirdPersonView != 1) {
                mc.gameSettings.thirdPersonView = 1;
            }
        }
    }
}