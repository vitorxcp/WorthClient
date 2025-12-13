package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Shadow private Minecraft mc;

    /**
     * @author vitorxp
     * @reason Animação?
     */
    @Inject(method = "setupTerrain", at = @At("HEAD"))
    private void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        if (WorthClient.PerspectiveModToggle && viewEntity == mc.getRenderViewEntity()) {
            //viewEntity.rotationYaw = PerspectiveMod.cameraYaw;
            //viewEntity.rotationPitch = PerspectiveMod.cameraPitch;
            //viewEntity.prevRotationYaw = PerspectiveMod.cameraYaw;
            //viewEntity.prevRotationPitch = PerspectiveMod.cameraPitch;
        }
    }

    @Inject(method = "setupTerrain", at = @At("RETURN"))
    private void setupTerrainReturn(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        if (WorthClient.PerspectiveModToggle && viewEntity == mc.getRenderViewEntity()) {}
    }
}