package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.config.PerfConfig;
import com.vitorxp.WorthClient.utils.math.Mth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class WaveyCapeRenderer {

    public static void render(AbstractClientPlayer entity, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        double distSq = entity.getDistanceSqToEntity(mc.thePlayer);
        if (entity != mc.thePlayer && distSq > (20 * 20)) { // 20 blocos de distância
            return;
        }

        boolean lowPerformanceMode = Minecraft.getDebugFPS() < 30;

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0D, 0.0D, 0.125D);

        double d0 = entity.prevChasingPosX + (entity.chasingPosX - entity.prevChasingPosX) * (double)partialTicks - (entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks);
        double d1 = entity.prevChasingPosY + (entity.chasingPosY - entity.prevChasingPosY) * (double)partialTicks - (entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks);
        double d2 = entity.prevChasingPosZ + (entity.chasingPosZ - entity.prevChasingPosZ) * (double)partialTicks - (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks);

        float f = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
        double d3 = (double)Mth.sin(f * (float)Math.PI / 180.0F); // Usando Fast Math
        double d4 = (double)(-Mth.cos(f * (float)Math.PI / 180.0F)); // Usando Fast Math

        float f1 = (float)d1 * 10.0F;
        f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);

        float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;

        if (f2 < 0.0F) f2 = 0.0F;

        float f4 = lowPerformanceMode ? 0 : entity.prevCameraYaw + (entity.cameraYaw - entity.prevCameraYaw) * partialTicks;
        f1 = f1 + Mth.sin((entity.prevDistanceWalkedModified + (entity.distanceWalkedModified - entity.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

        if (entity.isSneaking()) {
            f1 += 25.0F;
        }

        GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.popMatrix();
    }
}