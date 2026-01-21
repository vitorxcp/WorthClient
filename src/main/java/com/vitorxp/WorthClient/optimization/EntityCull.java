package com.vitorxp.WorthClient.optimization;

import com.vitorxp.WorthClient.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityCull {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        if (!PerfConfig.entityCull) return;

        Entity e = event.entity;
        if (e == null || mc.thePlayer == null || mc.theWorld == null) return;
        if (e == mc.thePlayer) return;

        double distSq = mc.thePlayer.getDistanceSqToEntity(e);

        double max = PerfConfig.entityCullDistance;
        double maxSq = max * max;

        if (distSq > (maxSq * 1.5)) {
            event.setCanceled(true);
            return;
        }

        if (PerfConfig.cullArmorStands && e instanceof EntityArmorStand && distSq > maxSq) {
            event.setCanceled(true);
            return;
        }

        if (PerfConfig.cullDroppedItems && e instanceof EntityItem && distSq > maxSq) {
            event.setCanceled(true);
        }
    }
}