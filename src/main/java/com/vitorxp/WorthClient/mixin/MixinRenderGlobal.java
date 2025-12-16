package com.vitorxp.WorthClient.mixin;

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

    // Backup das variáveis
    private float originalYaw;
    private float originalPitch;
    private float originalPrevYaw;
    private float originalPrevPitch;

    /**
     * @author vitorxp
     * @reason Versão Estável - Baseada no código original que você tinha.
     * Altera a rotação apenas durante o Culling e restaura logo em seguida.
     */
    @Inject(method = "setupTerrain", at = @At("HEAD"))
    private void setupTerrainHead(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && viewEntity == mc.getRenderViewEntity()) {
            originalYaw = viewEntity.rotationYaw;
            originalPitch = viewEntity.rotationPitch;
            originalPrevYaw = viewEntity.prevRotationYaw;
            originalPrevPitch = viewEntity.prevRotationPitch;

            viewEntity.rotationYaw = PerspectiveMod.cameraYaw;
            viewEntity.rotationPitch = PerspectiveMod.cameraPitch;
            viewEntity.prevRotationYaw = PerspectiveMod.cameraYaw;
            viewEntity.prevRotationPitch = PerspectiveMod.cameraPitch;
        }
    }

    @Inject(method = "setupTerrain", at = @At("RETURN"))
    private void setupTerrainReturn(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && viewEntity == mc.getRenderViewEntity()) {
            viewEntity.rotationYaw = originalYaw;
            viewEntity.rotationPitch = originalPitch;
            viewEntity.prevRotationYaw = originalPrevYaw;
            viewEntity.prevRotationPitch = originalPrevPitch;
        }
    }
}