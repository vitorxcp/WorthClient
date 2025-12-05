package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.WorthClient;
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

    @Shadow @Final
    private Session session;

    @Inject(method = "getSession", at = @At("HEAD"), cancellable = true)
    private void overrideSession(CallbackInfoReturnable<Session> cir) {
        if (WorthClient.modoOfflineAtivo) {
            String name = this.session.getUsername();
            String uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes()).toString();

            Session offline = new Session(
                    name,
                    uuid,
                    "invalid",
                    "legacy"
            );

            cir.setReturnValue(offline);
        }
    }
}
