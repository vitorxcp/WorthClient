package com.vitorxp.SkyBlockModVX.optimization;

import com.vitorxp.SkyBlockModVX.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.LinkedList;
import java.util.Queue;

public class LightOptimizer {

    private static final Queue<BlockPos> lightQueue = new LinkedList<>();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        if (!PerfConfig.lightOptEnabled) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null) return;

        int updatesPerTick = PerfConfig.lightUpdatesPerTick;

        for (int i = 0; i < updatesPerTick && !lightQueue.isEmpty(); i++) {
            BlockPos pos = lightQueue.poll();
            if (pos == null) continue;

            if (PerfConfig.skipVoidLight && pos.getY() < 130) continue;

            world.checkLightFor(EnumSkyBlock.BLOCK, pos);
            world.checkLightFor(EnumSkyBlock.SKY, pos);
        }
    }
}