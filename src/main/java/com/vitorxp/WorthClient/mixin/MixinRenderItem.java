package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    /**
     * @author vitorxp
     * @reason Corrige erro de OpenGL (1282/1283) verificando nulos antes de processar GL
     */
    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void safeRenderItem(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null || model == null) {
            ci.cancel();
            return;
        }

        if (stack.getItemDamage() < 0) {
            stack.setItemDamage(0);
        }
    }

    @Inject(method = "renderEffect", at = @At("RETURN"))
    private void cleanUpGlint(IBakedModel model, CallbackInfo ci) {
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }
}