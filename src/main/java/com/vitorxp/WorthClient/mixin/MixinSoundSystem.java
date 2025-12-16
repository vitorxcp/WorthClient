package com.vitorxp.WorthClient.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemLogger;

@Mixin(value = SoundSystem.class, remap = false)
public abstract class MixinSoundSystem {

    @Shadow(remap = false)
    protected SoundSystemLogger logger;

    @Inject(method = "message", at = @At("HEAD"), cancellable = true, remap = false)
    private void onMessage(String message, int indent, CallbackInfo ci) {
        if (message == null) return;

        if (message.contains("SoundSystem shutting down...")) {
            ci.cancel();
            this.logger.message("[WorthClient] Audio Engine: Desligando sistema de som...", 1);
        }

        if (message.contains("Starting up SoundSystem...")) {
            ci.cancel();
            this.logger.message("[WorthClient] Audio Engine: Iniciando sistema de som...", 1);
        }
        else if (message.contains("Author: Paul Lamb") || message.contains("www.paulscode.com")) {
            ci.cancel();

            if (this.logger != null) {
                this.logger.importantMessage("Sound Engine: WorthClient Audio System by vitorxp", 1);
            }
        }
    }

    @Inject(method = "importantMessage(Ljava/lang/String;I)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void onImportantMessage(String message, int indent, CallbackInfo ci) {
        if (message != null && message.contains("Author: Paul Lamb")) {
            ci.cancel();

            if (this.logger != null) {
                this.logger.importantMessage("Sound Engine: WorthClient Audio System by vitorxp", 1);
            }
        }
    }
}