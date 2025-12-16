package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer {

    @Inject(method = "setRotationAngles", at = @At("HEAD"))
    public void onSetRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled && entityIn == Minecraft.getMinecraft().thePlayer) {
            float yawDiff = PerspectiveMod.cameraYaw - entityIn.rotationYaw;

            while (yawDiff >= 180.0F) yawDiff -= 360.0F;
            while (yawDiff < -180.0F) yawDiff += 360.0F;

            float clampedYaw = MathHelper.clamp_float(yawDiff, -90.0F, 90.0F);

            ModelPlayer model = (ModelPlayer) (Object) this;
            model.bipedHead.rotateAngleY = clampedYaw * 0.017453292F;
            model.bipedHead.rotateAngleX = PerspectiveMod.cameraPitch * 0.017453292F;
            model.bipedHeadwear.rotateAngleY = model.bipedHead.rotateAngleY;
            model.bipedHeadwear.rotateAngleX = model.bipedHead.rotateAngleX;
        }
    }
}