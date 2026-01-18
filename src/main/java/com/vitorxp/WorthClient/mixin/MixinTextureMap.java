package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(TextureMap.class)
public class MixinTextureMap {

    @Shadow
    private int mipmapLevels;

    @Shadow
    private Map<String, TextureAtlasSprite> mapRegisteredSprites;

    @Inject(
            method = "loadTextureAtlas",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureUtil;allocateTextureImpl(IIII)V",
                    shift = At.Shift.AFTER
            )
    )
    private void preGenerateMipmaps(CallbackInfo ci) {
        if (this.mipmapLevels > 0 && this.mapRegisteredSprites != null) {
            long start = System.currentTimeMillis();
            System.out.println("[WorthClient] OTIMIZADOR: Gerando Mipmaps em paralelo para " + mapRegisteredSprites.size() + " sprites...");

            this.mapRegisteredSprites.values().parallelStream().forEach(sprite -> {
                try {
                    if (sprite.getFrameCount() > 0) {
                        sprite.generateMipmaps(this.mipmapLevels);
                    }
                } catch (Exception e) {}
            });

            System.out.println("[WorthClient] Mipmaps concluídos em " + (System.currentTimeMillis() - start) + "ms.");
        }
    }

    @Redirect(
            method = "loadTextureAtlas",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;generateMipmaps(I)V"
            )
    )
    private void safeMipmapGeneration(TextureAtlasSprite sprite, int level) {
        boolean isValid = false;

        try {
            if (sprite.getFrameCount() > 0) {
                int[][] frames = sprite.getFrameTextureData(0);

                if (frames != null && frames.length > level && frames[level] != null) {
                    isValid = true;
                }
            }
        } catch (Exception ignored) {}

        if (!isValid) {
            try {
                sprite.generateMipmaps(level);
            } catch (Throwable t) {
                System.err.println("[WorthClient] Erro fatal ao gerar textura de fallback: " + sprite.getIconName());
            }
        }
    }
}