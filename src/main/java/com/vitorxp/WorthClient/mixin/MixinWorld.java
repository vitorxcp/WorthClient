package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow public abstract IChunkProvider getChunkProvider();

    @Inject(method = "getWorldTime", at = @At("HEAD"), cancellable = true)
    public void onGetWorldTime(CallbackInfoReturnable<Long> cir) {
        if (WorthClient.timeChangerEnable) {
            cir.setReturnValue((long) WorthClient.clientTime);
        }
    }

    @Inject(method = "setWorldTime", at = @At("HEAD"), cancellable = true)
    public void onSetWorldTime(long time, CallbackInfo ci) {
        if (WorthClient.timeChangerEnable) {
            ci.cancel();
        }
    }

    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    public void optimizeVoidLight(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        int y = pos.getY();
        if (y < 0 || y >= 256) {
            cir.setReturnValue(false);
            return;
        }

        if (!this.getChunkProvider().chunkExists(pos.getX() >> 4, pos.getZ() >> 4)) {
            cir.setReturnValue(false);
        }
    }
}