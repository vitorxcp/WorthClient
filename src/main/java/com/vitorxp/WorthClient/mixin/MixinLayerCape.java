package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.socket.ClientSocket;
import com.vitorxp.WorthClient.utils.AnimatedCape;
import com.vitorxp.WorthClient.utils.CapeLoader;
import com.vitorxp.WorthClient.utils.WaveyCapeRenderer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LayerCape.class)
public class MixinLayerCape {
    @Shadow @Final private RenderPlayer playerRenderer;
    @Unique private final WaveyCapeRenderer waveyRenderer = new WaveyCapeRenderer();
    @Inject(method = "doRenderLayer", at = @At("HEAD"), cancellable = true)
    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        if (!entitylivingbaseIn.hasPlayerInfo() || entitylivingbaseIn.isInvisible() || !entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE)) {
            return;
        }
        String playerName = entitylivingbaseIn.getName().toLowerCase();
        AnimatedCape animCape = null;
        if (ClientSocket.playerCosmetics.containsKey(playerName)) {
            Set<String> cosmetics = ClientSocket.playerCosmetics.get(playerName);
            for (String cosmeticId : cosmetics) {
                animCape = CapeLoader.getAnimatedCape(cosmeticId);
                if (animCape != null) break;
            }
        }
        if (animCape != null) {
            ci.cancel();
            animCape.update();
            this.playerRenderer.bindTexture(animCape.getTexture());
            this.waveyRenderer.render(entitylivingbaseIn, partialTicks, animCape);
        }
    }
}