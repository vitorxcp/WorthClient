package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TextureOptimizer {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static int tickCounter = 0;

    private static final int CLEANUP_INTERVAL = 3600;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;

        if (tickCounter >= CLEANUP_INTERVAL) {
            tickCounter = 0;
            performMemoryCleanup();
        }
    }

    public static void optimizeAndReload() {
        performMemoryCleanup();
        mc.refreshResources();
    }

    private static void performMemoryCleanup() {
        if (mc.thePlayer != null) {
            mc.getTextureManager().tick();

            long maxMem = Runtime.getRuntime().maxMemory();
            long totalMem = Runtime.getRuntime().totalMemory();
            long freeMem = Runtime.getRuntime().freeMemory();
            long usedMem = totalMem - freeMem;

            if ((double)usedMem / maxMem > 0.90) {
                System.runFinalization();
                System.gc();
            }
        }
    }

    public static void optimizeTexture(ResourceLocation texture) {
        if (texture.getResourcePath().startsWith("textures/gui")) {
            // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            // GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }
}