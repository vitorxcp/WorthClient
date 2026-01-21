package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {

    @Inject(method = "renderEffect", at = @At("HEAD"), cancellable = true)
    private void onRenderEffect(IBakedModel model, CallbackInfo ci) {
        if (net.minecraft.client.Minecraft.getDebugFPS() < 30) {
            ci.cancel();
        }
    }

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null) {
            ci.cancel();
        }
    }
}