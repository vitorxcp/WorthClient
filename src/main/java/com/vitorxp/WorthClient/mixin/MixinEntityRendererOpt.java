package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRendererOpt {

    /**
     * @author vitorxp
     * @reason Otimização de Lightmap. Evita atualizações desnecessárias.
     */
    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
    private void onUpdateLightmap(float partialTicks, CallbackInfo ci) { }
}