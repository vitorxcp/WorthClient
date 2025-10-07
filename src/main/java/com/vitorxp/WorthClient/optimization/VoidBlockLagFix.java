package com.vitorxp.WorthClient.optimization;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class VoidBlockLagFix {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static final int VOID_HEIGHT = 100;
    private static final double MAX_RENDER_DISTANCE_SQ = 50 * 50;

    @SubscribeEvent
    public void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.world.isRemote && event.pos.getY() < VOID_HEIGHT) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderLivingSpecials(RenderLivingEvent.Specials.Pre<? extends EntityLivingBase> event) {
        try {
            EntityLivingBase ent = event.entity;
            if (ent == null || mc.thePlayer == null) return;

            double distSq = ent.getDistanceSqToEntity(mc.thePlayer);

            if (distSq > MAX_RENDER_DISTANCE_SQ) {
                event.setCanceled(true);
                return;
            }

            String className = ent.getClass().getSimpleName().toLowerCase();

            if (className.contains("armorstand") && ent.isInvisible() &&
                    (ent.getCustomNameTag() == null || ent.getCustomNameTag().isEmpty())) {
                event.setCanceled(true);
                return;
            }

            if (ent.isInvisible() && (ent.getCustomNameTag() == null || ent.getCustomNameTag().isEmpty())) {
                event.setCanceled(true);
            }

        } catch (Throwable ignored) {}
    }
}
