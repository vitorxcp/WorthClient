package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.config.AnimationsConfig;
import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    private float originalYaw;
    private float originalPitch;
    private float originalPrevYaw;
    private float originalPrevPitch;

    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void onOrientCameraHead(float partialTicks, CallbackInfo ci) {
        Entity entity = this.mc.getRenderViewEntity();

        if (PerspectiveMod.perspectiveToggled && entity != null) {
            originalYaw = entity.rotationYaw;
            originalPitch = entity.rotationPitch;
            originalPrevYaw = entity.prevRotationYaw;
            originalPrevPitch = entity.prevRotationPitch;

            entity.rotationYaw = PerspectiveMod.cameraYaw;
            entity.rotationPitch = PerspectiveMod.cameraPitch;
            entity.prevRotationYaw = PerspectiveMod.prevCameraYaw;
            entity.prevRotationPitch = PerspectiveMod.prevCameraPitch;
        }
    }

    @Inject(method = "orientCamera", at = @At("RETURN"))
    private void onOrientCameraReturn(float partialTicks, CallbackInfo ci) {
        Entity entity = this.mc.getRenderViewEntity();

        if (PerspectiveMod.perspectiveToggled && entity != null) {
            entity.rotationYaw = originalYaw;
            entity.rotationPitch = originalPitch;
            entity.prevRotationYaw = originalPrevYaw;
            entity.prevRotationPitch = originalPrevPitch;
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void onHurtCameraEffect(float partialTicks, CallbackInfo ci) {
        if (AnimationsConfig.enabled && !AnimationsConfig.damageShake) {
            ci.cancel();
        }
    }
}