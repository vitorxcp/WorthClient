package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.keybinds.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.Display;

public class PerspectiveMod {

    private final Minecraft mc;
    public static boolean perspectiveToggled = false;

    // Estas variáveis são modificadas diretamente pelo MixinMouseHelper agora!
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

        // Salva a visão anterior (primeira ou terceira pessoa)
        previousThirdPersonView = mc.gameSettings.thirdPersonView;

        // Força terceira pessoa (Back)
        mc.gameSettings.thirdPersonView = 1;

        if (mc.thePlayer != null) {
            // Sincroniza a câmera com a rotação atual do player para não dar "pulo"
            cameraPitch = mc.thePlayer.rotationPitch;

            if (WorthClient.PerspectiveStartFront) {
                cameraYaw = mc.thePlayer.rotationYaw + 180f;
            } else {
                cameraYaw = mc.thePlayer.rotationYaw;
            }
        }
    }

    public void disable() {
        if (!perspectiveToggled) return;
        perspectiveToggled = false;

        // Restaura a visão original
        mc.gameSettings.thirdPersonView = previousThirdPersonView;
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // Lógica de Toggle (Segurar ou Apertar)
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

        // Força a terceira pessoa enquanto o mod estiver ligado
        if (perspectiveToggled && mc.gameSettings.thirdPersonView != 1) {
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    // REMOVIDO: onRenderTick e handleMouseMovement
    // Motivo: Isso agora é feito no MixinMouseHelper para máxima performance e compatibilidade com OptiFine.

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (perspectiveToggled) {
            // Isso ajuda mods externos a saberem para onde estamos olhando,
            // embora o MixinEntityRenderer já faça o trabalho visual principal.
            event.yaw = cameraYaw;
            event.pitch = cameraPitch;
        }
    }
}