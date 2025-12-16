package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
    public float getRotationYaw(Entity entity) {
        return (PerspectiveMod.perspectiveToggled && entity == mc.thePlayer) ? PerspectiveMod.cameraYaw : entity.rotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F"))
    public float getPrevRotationYaw(Entity entity) {
        return (PerspectiveMod.perspectiveToggled && entity == mc.thePlayer) ? PerspectiveMod.cameraYaw : entity.prevRotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
    public float getRotationPitch(Entity entity) {
        return (PerspectiveMod.perspectiveToggled && entity == mc.thePlayer) ? PerspectiveMod.cameraPitch : entity.rotationPitch;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
    public float getPrevRotationPitch(Entity entity) {
        return (PerspectiveMod.perspectiveToggled && entity == mc.thePlayer) ? PerspectiveMod.cameraPitch : entity.prevRotationPitch;
    }
}