package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.S29PacketSoundEffect; // <--- Importante
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject; // <--- Importante
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo; // <--- Importante

import java.util.UUID;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {

    @Redirect(method = "handleSpawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;getPlayerInfo(Ljava/util/UUID;)Lnet/minecraft/client/network/NetworkPlayerInfo;"))
    public NetworkPlayerInfo onGetPlayerInfo(NetHandlerPlayClient instance, UUID uniqueId) {
        NetworkPlayerInfo info = instance.getPlayerInfo(uniqueId);
        if (info == null) {
            return new NetworkPlayerInfo(new com.mojang.authlib.GameProfile(uniqueId, "Unknown"));
        }
        return info;
    }

    @Inject(method = "handleSoundEffect", at = @At("HEAD"), cancellable = true)
    public void onHandleSoundEffect(S29PacketSoundEffect packet, CallbackInfo ci) {
        String name = packet.getSoundName();
        if (name != null && (name.equals("minecraft:damage.thorns") || name.equals("damage.thorns"))) {
            ci.cancel();
        }
    }
}