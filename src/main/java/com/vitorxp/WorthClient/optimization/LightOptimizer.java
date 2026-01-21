package com.vitorxp.WorthClient.optimization;

import com.vitorxp.WorthClient.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LightOptimizer {

    private static int updatesThisTick = 0;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        updatesThisTick = 0;
    }

    public static boolean shouldUpdateLight() {
        if (!PerfConfig.lightOptEnabled) return true;

        int limit = (Minecraft.getDebugFPS() < 20) ? 3 : PerfConfig.lightUpdatesPerTick;

        if (updatesThisTick >= limit) {
            return false;
        }

        updatesThisTick++;
        return true;
    }
}