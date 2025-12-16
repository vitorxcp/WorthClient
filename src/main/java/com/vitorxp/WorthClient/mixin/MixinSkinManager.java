package com.vitorxp.WorthClient.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinManager.class)
public class MixinSkinManager {

    /**
     * @author vitorxp
     * @reason Corrige lag de skin (Argumentos corrigidos para evitar crash)
     */
    @Inject(method = "loadSkin", at = @At("HEAD"), cancellable = true)
    public void onLoadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, CallbackInfoReturnable<ResourceLocation> cir) {
        if (profileTexture == null) {
            cir.setReturnValue(null);
        }
    }
}