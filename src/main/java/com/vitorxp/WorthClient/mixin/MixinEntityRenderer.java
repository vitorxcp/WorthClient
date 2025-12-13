package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.EntityLivingBase;
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
    private float originalHeadYaw;
    private float originalPrevHeadYaw;
    private float originalRenderYawOffset;
    private float originalPrevRenderYawOffset;

    /**
     * @author vitorxp
     * @reason Correção RIGOROSA de X-Ray e Colisão
     */
    @Inject(method = "orientCamera", at = @At("HEAD"))
    private void onOrientCameraHead(float partialTicks, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && mc.getRenderViewEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.getRenderViewEntity();

            originalYaw = entity.rotationYaw;
            originalPrevYaw = entity.prevRotationYaw;
            originalPitch = entity.rotationPitch;
            originalPrevPitch = entity.prevRotationPitch;

            originalHeadYaw = entity.rotationYawHead;
            originalPrevHeadYaw = entity.prevRotationYawHead;
            originalRenderYawOffset = entity.renderYawOffset;
            originalPrevRenderYawOffset = entity.prevRenderYawOffset;

            float yaw = PerspectiveMod.cameraYaw;
            float pitch = PerspectiveMod.cameraPitch;

            entity.rotationYaw = yaw;
            entity.prevRotationYaw = yaw;
            entity.rotationPitch = pitch;
            entity.prevRotationPitch = pitch;

            entity.rotationYawHead = yaw;
            entity.prevRotationYawHead = yaw;
            entity.renderYawOffset = yaw;
            entity.prevRenderYawOffset = yaw;
        }
    }

    @Inject(method = "orientCamera", at = @At("RETURN"))
    private void onOrientCameraReturn(float partialTicks, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && mc.getRenderViewEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) mc.getRenderViewEntity();

            entity.rotationYaw = originalYaw;
            entity.prevRotationYaw = originalPrevYaw;
            entity.rotationPitch = originalPitch;
            entity.prevRotationPitch = originalPrevPitch;

            entity.rotationYawHead = originalHeadYaw;
            entity.prevRotationYawHead = originalPrevHeadYaw;
            entity.renderYawOffset = originalRenderYawOffset;
            entity.prevRenderYawOffset = originalPrevRenderYawOffset;
        }
    }

    @Redirect(
            method = {"updateCameraAndRender", "func_78480_b"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDX()I"),
            require = 0
    )
    public int getDX() {
        return PerspectiveMod.perspectiveToggled ? 0 : Mouse.getDX();
    }

    @Redirect(
            method = {"updateCameraAndRender", "func_78480_b"},
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;getDY()I"),
            require = 0
    )
    public int getDY() {
        return PerspectiveMod.perspectiveToggled ? 0 : Mouse.getDY();
    }
}