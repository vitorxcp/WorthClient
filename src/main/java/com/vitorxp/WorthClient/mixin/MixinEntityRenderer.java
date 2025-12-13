package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    private float originalYaw;
    private float originalPrevYaw;
    private float originalPitch;
    private float originalPrevPitch;

    /**
     * @author vitorxp
     * @reason Correção de Câmera/X-Ray sem quebrar cabeças/capas
     */
    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void onOrientCameraHead(float partialTicks, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && mc.getRenderViewEntity() != null) {
            Entity entity = mc.getRenderViewEntity();

            originalYaw = entity.rotationYaw;
            originalPrevYaw = entity.prevRotationYaw;
            originalPitch = entity.rotationPitch;
            originalPrevPitch = entity.prevRotationPitch;

            entity.rotationYaw = PerspectiveMod.cameraYaw;
            entity.prevRotationYaw = PerspectiveMod.cameraYaw;
            entity.rotationPitch = PerspectiveMod.cameraPitch;
            entity.prevRotationPitch = PerspectiveMod.cameraPitch;
        }
    }

    @Inject(method = "orientCamera", at = @At("RETURN"))
    private void onOrientCameraReturn(float partialTicks, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && mc.getRenderViewEntity() != null) {
            Entity entity = mc.getRenderViewEntity();

            entity.rotationYaw = originalYaw;
            entity.prevRotationYaw = originalPrevYaw;
            entity.rotationPitch = originalPitch;
            entity.prevRotationPitch = originalPrevPitch;
        }
    }

    @Redirect(method = {"updateCameraAndRender", "func_78480_b"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDX()I"), require = 0)
    public int getDX() {
        return PerspectiveMod.perspectiveToggled ? 0 : Mouse.getDX();
    }

    @Redirect(method = {"updateCameraAndRender", "func_78480_b"}, at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDY()I"), require = 0)
    public int getDY() {
        return PerspectiveMod.perspectiveToggled ? 0 : Mouse.getDY();
    }
}