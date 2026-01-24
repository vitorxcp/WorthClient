package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.gui.GuiStreamIndicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiStreamIndicator.class)
public class MixinGuiStreamIndicator {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(int p_152437_1_, int p_152437_2_, CallbackInfo ci) {
        ci.cancel();
    }
}