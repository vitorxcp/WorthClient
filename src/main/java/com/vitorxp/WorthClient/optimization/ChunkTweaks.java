package com.vitorxp.WorthClient.optimization;

import com.vitorxp.WorthClient.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ChunkTweaks {
    private int ticker = 0;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        if (++ticker > 6000) {
            ticker = 0;
            if (mc.thePlayer != null && PerfConfig.autoMemoryClean) {
                System.runFinalization();
            }
        }
    }
}