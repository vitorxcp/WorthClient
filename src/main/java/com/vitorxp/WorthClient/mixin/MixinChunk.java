package com.vitorxp.WorthClient.mixin;

import net.minecraft.util.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public class MixinChunk {

    @Shadow private ExtendedBlockStorage[] storageArrays;

    @Inject(method = "getLightFor", at = @At("HEAD"), cancellable = true)
    public void getLightForFast(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        int y = pos.getY();
        if (y < 0 || y >= 256) {
            cir.setReturnValue(type.defaultLightValue);
            return;
        }

        ExtendedBlockStorage storage = this.storageArrays[y >> 4];

        if (storage == null) {
            if (type == EnumSkyBlock.BLOCK) {
                cir.setReturnValue(0);
            } else {
                cir.setReturnValue(15);
            }
        }
    }

    @Inject(method = "setLightFor", at = @At("HEAD"), cancellable = true)
    public void setLightForFast(EnumSkyBlock type, BlockPos pos, int value, CallbackInfo ci) {
        int y = pos.getY();
        if (y < 0 || y >= 256) {
            ci.cancel();
            return;
        }

        ExtendedBlockStorage storage = this.storageArrays[y >> 4];
        if (storage == null) {
            ci.cancel();
        }
    }
}