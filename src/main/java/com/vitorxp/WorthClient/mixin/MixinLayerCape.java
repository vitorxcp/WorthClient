package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.socket.ClientSocket;
import com.vitorxp.WorthClient.utils.AnimatedCape;
import com.vitorxp.WorthClient.utils.CapeLoader;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LayerCape.class)
public class MixinLayerCape {
    @Shadow @Final private RenderPlayer playerRenderer;
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        if (!entitylivingbaseIn.hasPlayerInfo() || entitylivingbaseIn.isInvisible() || !entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE)) {
            return;
        }
        String playerName = entitylivingbaseIn.getName().toLowerCase();
        AnimatedCape animCape = null;
        if (ClientSocket.playerCosmetics.containsKey(playerName)) {
            Set<String> cosmetics = ClientSocket.playerCosmetics.get(playerName);
            for (String cosmeticId : cosmetics) {
                animCape = CapeLoader.getAnimatedCape(cosmeticId);
                if (animCape != null) break;
            }
        }
        if (animCape != null) {
            try {
                animCape.update();
                ci.cancel();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.playerRenderer.bindTexture(animCape.getTexture());
                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, 0.0F, 0.125F);
                if (entitylivingbaseIn.isSneaking()) {
                    GlStateManager.translate(0.0F, 0.2F, -0.05F);
                }
                double d0 = entitylivingbaseIn.prevChasingPosX + (entitylivingbaseIn.chasingPosX - entitylivingbaseIn.prevChasingPosX) * (double)partialTicks - (entitylivingbaseIn.prevPosX + (entitylivingbaseIn.posX - entitylivingbaseIn.prevPosX) * (double)partialTicks);
                double d1 = entitylivingbaseIn.prevChasingPosY + (entitylivingbaseIn.chasingPosY - entitylivingbaseIn.prevChasingPosY) * (double)partialTicks - (entitylivingbaseIn.prevPosY + (entitylivingbaseIn.posY - entitylivingbaseIn.prevPosY) * (double)partialTicks);
                double d2 = entitylivingbaseIn.prevChasingPosZ + (entitylivingbaseIn.chasingPosZ - entitylivingbaseIn.prevChasingPosZ) * (double)partialTicks - (entitylivingbaseIn.prevPosZ + (entitylivingbaseIn.posZ - entitylivingbaseIn.prevPosZ) * (double)partialTicks);
                float f = entitylivingbaseIn.prevRenderYawOffset + (entitylivingbaseIn.renderYawOffset - entitylivingbaseIn.prevRenderYawOffset) * partialTicks;
                double d3 = (double)MathHelper.sin(f * (float)Math.PI / 180.0F);
                double d4 = (double)(-MathHelper.cos(f * (float)Math.PI / 180.0F));
                float f1 = (float)d1 * 10.0F;
                f1 = MathHelper.clamp_float(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
                f3 *= 2.5F;
                if (f2 < 0.0F) f2 = 0.0F;
                float f4 = entitylivingbaseIn.prevCameraYaw + (entitylivingbaseIn.cameraYaw - entitylivingbaseIn.prevCameraYaw) * partialTicks;
                f1 = f1 + MathHelper.sin((entitylivingbaseIn.prevDistanceWalkedModified + (entitylivingbaseIn.distanceWalkedModified - entitylivingbaseIn.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4 * 1.5F;
                double time = (double)System.currentTimeMillis() / 15.0;
                float waveBase = (float)Math.sin(time / 20.0) * 4.0F;
                float waveSide = (float)Math.cos(time / 25.0) * 3.0F;
                float waveFlutter = (float)Math.sin(time / 10.0) * 1.5F;
                float speedDamp = 1.0F - MathHelper.clamp_float(f2 / 40.0F, 0.0F, 0.7F);
                float finalWaveX = (waveBase + waveFlutter) * speedDamp;
                float finalWaveZ = waveSide * speedDamp;
                if (d1 < 0) f1 += (-d1 * 40.0F);
                if (entitylivingbaseIn.isSneaking()) {
                    f1 += 25.0F;
                    GlStateManager.translate(0.0F, 0.15F, -0.05F);
                }
                GlStateManager.rotate(6.0F + f2 / 2.0F + f1 + finalWaveX, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(f3 / 2.0F + finalWaveZ, 0.0F, 0.0F, 1.0F);
                GlStateManager.rotate(-f3 / 2.0F + (finalWaveZ * 0.5F), 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                if (animCape.getTotalFrames() > 0) {
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.pushMatrix();
                    GlStateManager.loadIdentity();
                    float frameHeight = 1.0F / (float)animCape.getTotalFrames();
                    GlStateManager.translate(0.0F, (float)animCape.getCurrentFrame() * frameHeight, 0.0F);
                    GlStateManager.scale(1.0F, frameHeight, 1.0F);
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                    this.playerRenderer.getMainModel().renderCape(0.0625F);
                    GlStateManager.matrixMode(GL11.GL_TEXTURE);
                    GlStateManager.popMatrix();
                    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
                } else {
                    this.playerRenderer.getMainModel().renderCape(0.0625F);
                }
                GlStateManager.popMatrix();
            } catch (Exception e) { }
        }
    }
}