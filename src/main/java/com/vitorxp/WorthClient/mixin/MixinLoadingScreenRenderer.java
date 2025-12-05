package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.gui.LoadingScreenHook;
import net.minecraft.client.LoadingScreenRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingScreenRenderer.class)
public class MixinLoadingScreenRenderer {

    @Inject(method = "displayLoadingString", at = @At("HEAD"), cancellable = true)
    private void hookDisplayText(String text, CallbackInfo ci) {
        if (LoadingScreenHook.customGUI != null) {
            LoadingScreenHook.customGUI.update(text, LoadingScreenHook.customGUI.progress);
            ci.cancel();
        }
    }

    @Inject(method = "setLoadingProgress", at = @At("HEAD"), cancellable = true)
    private void hookProgress(int progress, CallbackInfo ci) {
        if (LoadingScreenHook.customGUI != null) {
            float p = Math.min(1f, Math.max(0f, progress / 100f));
            LoadingScreenHook.customGUI.update(LoadingScreenHook.customGUI.text, p);
            ci.cancel();
        }
    }
}