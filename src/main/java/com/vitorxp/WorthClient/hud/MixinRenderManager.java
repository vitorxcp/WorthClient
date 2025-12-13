package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.utils.PerspectiveMod;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    @Shadow public float playerViewY;
    @Shadow public float playerViewX;

    /**
     * @author vitorxp
     * @reason Faz hologramas, tags e partículas olharem para a câmera do Perspective Mod
     */
    @Inject(method = "cacheActiveRenderInfo", at = @At("RETURN"))
    private void onCacheActiveRenderInfo(World worldIn, FontRenderer textRendererIn, Entity livingPlayerIn, Entity pointedEntityIn, GameSettings optionsIn, float partialTicks, CallbackInfo ci) {
        if (PerspectiveMod.perspectiveToggled) {
            this.playerViewY = PerspectiveMod.cameraYaw + 180.0F;

            this.playerViewX = PerspectiveMod.cameraPitch;
        }
    }
}