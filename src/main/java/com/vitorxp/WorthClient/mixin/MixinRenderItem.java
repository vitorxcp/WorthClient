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

    @Inject(method = "renderItem(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V", at = @At("HEAD"), cancellable = true)
    public void limitItemRendering(ItemStack stack, IBakedModel model, CallbackInfo ci) {
        if (stack == null || stack.getItem() == null || model == null) {
            ci.cancel();
        }
    }
}