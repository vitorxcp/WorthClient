package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Final
    @Shadow
    private Session session;

    @Inject(method = "getSession", at = @At("HEAD"), cancellable = true)
    private void getSession(CallbackInfoReturnable<Session> cir) {
        if (com.vitorxp.WorthClient.WorthClient.modoOfflineAtivo) {
            Session sessaoOffline = new Session(
                    this.session.getUsername(),
                    UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.session.getUsername()).getBytes()).toString(),
                    "invalid",
                    "legacy"
            );

            cir.setReturnValue(sessaoOffline);
        }
    }
}