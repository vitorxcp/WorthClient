package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.utils.Skin3DLayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {

    @Inject(method = "renderRightArm", at = @At("RETURN"))
    public void onRenderRightArm(AbstractClientPlayer clientPlayer, CallbackInfo ci) {
        if (!WorthClient.skin3D) return;

        RenderPlayer renderPlayer = (RenderPlayer) (Object) this;
        Skin3DLayer.renderFirstPersonArm(renderPlayer, clientPlayer);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/renderer/entity/RenderManager;Z)V", at = @At("RETURN"))
    public void onInit(RenderManager renderManager, boolean useSmallArms, CallbackInfo ci) {
        RenderPlayer renderPlayer = (RenderPlayer) (Object) this;
        renderPlayer.addLayer(new Skin3DLayer(renderPlayer));
    }
}