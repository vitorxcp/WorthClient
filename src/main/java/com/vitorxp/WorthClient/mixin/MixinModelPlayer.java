package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPlayer.class)
public class MixinModelPlayer extends ModelBiped {

    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    public void onSetRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof AbstractClientPlayer) {

            if (WorthClient.skin3D) {
                this.bipedHeadwear.showModel = false;
                try {
                    ModelPlayer mp = (ModelPlayer) (Object) this;
                    mp.bipedBodyWear.showModel = false;
                    mp.bipedLeftArmwear.showModel = false;
                    mp.bipedRightArmwear.showModel = false;
                    mp.bipedLeftLegwear.showModel = false;
                    mp.bipedRightLegwear.showModel = false;
                } catch (Exception e) {
                }
            } else {
                this.bipedHeadwear.showModel = true;
                try {
                    ModelPlayer mp = (ModelPlayer) (Object) this;
                    mp.bipedBodyWear.showModel = true;
                    mp.bipedLeftArmwear.showModel = true;
                    mp.bipedRightArmwear.showModel = true;
                    mp.bipedLeftLegwear.showModel = true;
                    mp.bipedRightLegwear.showModel = true;
                } catch (Exception e) {
                }
            }
        }
    }
}