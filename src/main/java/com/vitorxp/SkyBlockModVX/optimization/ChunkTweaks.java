package com.vitorxp.SkyBlockModVX.optimization;

import com.vitorxp.SkyBlockModVX.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class ChunkTweaks {
    private int ticker = 0;


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (!PerfConfig.autoRenderDistance) return;
        if (e.phase != TickEvent.Phase.END) return;

        if (++ticker % 100 != 0) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        int fps = Minecraft.getDebugFPS();
        int current = mc.gameSettings.renderDistanceChunks;

        int min = PerfConfig.minRenderDistance;
        int target = PerfConfig.fpsTarget;

        if (fps < target && current > min) {
            //mc.gameSettings.renderDistanceChunks = current - 1;
        }
    }
}