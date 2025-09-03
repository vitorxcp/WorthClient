package com.vitorxp.SkyBlockModVX.optimization;

import com.vitorxp.SkyBlockModVX.config.PerfConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


public class EntityCull {


    @SubscribeEvent
    public void onRenderLivingPre(RenderLivingEvent.Pre event) {
        Entity e = event.entity;
        if (e == null) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null) return;


        double distSq = mc.thePlayer.getDistanceSqToEntity(e);
        double max = PerfConfig.entityCullDistance;
        double maxSq = max * max;


        if (PerfConfig.cullArmorStands && e instanceof EntityArmorStand && distSq > maxSq) {
            event.setCanceled(true);
            return;
        }
        if (PerfConfig.cullDroppedItems && e instanceof EntityItem && distSq > maxSq) {
            event.setCanceled(true);
        }
    }
}