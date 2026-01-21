package com.vitorxp.WorthClient.optimization;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MinecraftOptimizer {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static final int VOID_Y_THRESHOLD = 0;

    @SubscribeEvent
    public void onBlockUpdate(BlockEvent.NeighborNotifyEvent event) {
        if (event.world != null && event.world.isRemote && event.pos.getY() < VOID_Y_THRESHOLD) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderLivingSpecialsPre(RenderLivingEvent.Specials.Pre<? extends EntityLivingBase> event) {
        EntityLivingBase ent = event.entity;

        if (ent == null || mc.thePlayer == null || ent == mc.thePlayer) return;

        double distSq = ent.getDistanceSqToEntity(mc.thePlayer);
        if (distSq > (40 * 40)) {
            event.setCanceled(true);
            return;
        }

        if (ent instanceof EntityArmorStand) {
            if (ent.isInvisible() && !ent.hasCustomName()) {
                event.setCanceled(true);
            }
        }
    }
}