package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.world.WorldProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldProvider.class)
public class MixinWorldProvider {

    @Inject(method = "calculateCelestialAngle", at = @At("HEAD"), cancellable = true)
    public void onCalculateCelestialAngle(long worldTime, float partialTicks, CallbackInfoReturnable<Float> cir) {
        if (WorthClient.timeChangerEnable) {
            long time = (long) WorthClient.clientTime;
            int j = (int)(time % 24000L);
            float f = ((float)j + partialTicks) / 24000.0F - 0.25F;

            if (f < 0.0F) f += 1.0F;
            if (f > 1.0F) f -= 1.0F;

            float f1 = 1.0F - (float)((Math.cos((double)f * Math.PI) + 1.0D) / 2.0D);
            f = f + (f1 - f) / 3.0F;

            cir.setReturnValue(f);
        }
    }
}