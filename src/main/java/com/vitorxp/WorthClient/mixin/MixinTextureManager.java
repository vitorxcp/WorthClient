package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureManager.class)
public abstract class MixinTextureManager {

    @Shadow public abstract void bindTexture(ResourceLocation resource);

    private ResourceLocation lastBoundTexture;

    /**
     * @author vitorxp
     * @reason Evita chamadas OpenGL desnecessárias (bindTexture)
     */
    @Inject(method = "bindTexture", at = @At("HEAD"), cancellable = true)
    public void onBindTexture(ResourceLocation resource, CallbackInfo ci) {
        if (resource != null && resource.equals(this.lastBoundTexture)) {
            ci.cancel();
            return;
        }
        this.lastBoundTexture = resource;
    }
}