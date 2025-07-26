package com.vitorxp.SkyBlockModVX.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LagManager {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static long lastTime = System.currentTimeMillis();
    private static int tickCount = 0;

    private static boolean settingsReduced = false;

    // Backup original
    private static int previousParticleSetting = -1;
    private static int previousRenderDistance = -1;
    private static boolean previousFancyGraphics = true;
    private static int previousSmoothLighting = -1;
    private static int previousClouds = -1;
    private static boolean previousViewBobbing = true;
    private static boolean previousEntityShadows = true;
    private static boolean previousTooltips = true;
    private static boolean previousAnaglyph = false;
    private static boolean previousUseVbo = false;
    private static boolean previousFboEnable = false;
    private static float previousChatOpacity = 1.0F;

    private static int lowTPSCounter = 0;
    private static int highTPSCounter = 0;

    public static void onTick(TickEvent.ClientTickEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (event.phase != TickEvent.Phase.END) return;

        tickCount++;

        if (tickCount >= 20) {
            long now = System.currentTimeMillis();
            long elapsed = now - lastTime;
            lastTime = now;

            float tps = 1000f * 20 / elapsed;
            int fps = Minecraft.getDebugFPS();
            GameSettings settings = mc.gameSettings;

            boolean lagDetected = (tps < 17.0 || fps < 30);

            if (lagDetected) {
                lowTPSCounter++;
                highTPSCounter = 0;
            } else {
                highTPSCounter++;
                lowTPSCounter = 0;
            }

            if (lowTPSCounter >= 3 && !settingsReduced) {
                previousParticleSetting = settings.particleSetting;
                previousRenderDistance = settings.renderDistanceChunks;
                previousFancyGraphics = settings.fancyGraphics;
                previousSmoothLighting = settings.ambientOcclusion;
                previousClouds = settings.clouds;
                previousViewBobbing = settings.viewBobbing;
                previousEntityShadows = settings.entityShadows;
                previousTooltips = settings.heldItemTooltips;
                previousAnaglyph = settings.anaglyph;
                previousUseVbo = settings.useVbo;
                previousFboEnable = settings.fboEnable;
                previousChatOpacity = settings.chatOpacity;

                settings.particleSetting = 2;
                settings.renderDistanceChunks = Math.max(4, settings.renderDistanceChunks / 2);
                settings.fancyGraphics = false;
                settings.ambientOcclusion = 0;
                settings.clouds = 0;
                settings.viewBobbing = false;
                settings.entityShadows = false;
                settings.heldItemTooltips = false;
                settings.anaglyph = false;
                settings.useVbo = true;
                settings.fboEnable = true;

                mc.renderGlobal.loadRenderers();

                settingsReduced = true;
                send("§cDesempenho baixo detectado! Ajustes gráficos temporários aplicados.");
                playNotificationSound();
            }

            if (settingsReduced && highTPSCounter >= 10) {
                if (previousParticleSetting != -1) settings.particleSetting = previousParticleSetting;
                if (previousRenderDistance != -1) settings.renderDistanceChunks = previousRenderDistance;
                settings.fancyGraphics = previousFancyGraphics;
                if (previousSmoothLighting != -1) settings.ambientOcclusion = previousSmoothLighting;
                if (previousClouds != -1) settings.clouds = previousClouds;

                settings.viewBobbing = previousViewBobbing;
                settings.entityShadows = previousEntityShadows;
                settings.heldItemTooltips = previousTooltips;
                settings.anaglyph = previousAnaglyph;
                settings.useVbo = previousUseVbo;
                settings.fboEnable = previousFboEnable;
                settings.chatOpacity = previousChatOpacity;

                mc.renderGlobal.loadRenderers();

                settingsReduced = false;
                send("§aDesempenho estabilizado! Configurações restauradas.");
                playNotificationSound();
            }

            tickCount = 0;
        }
    }

    private static void send(String msg) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§6[SkyBlockModVX] §r" + msg));
        }
    }

    private static void playNotificationSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("random.orb"), 1.0F));
    }
}