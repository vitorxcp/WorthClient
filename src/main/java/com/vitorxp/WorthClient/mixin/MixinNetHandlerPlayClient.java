package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.server.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Shadow private Minecraft gameController;
    @Shadow private WorldClient clientWorldController;

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

    @Inject(method = "handleScoreboardObjective", at = @At("HEAD"), cancellable = true)
    private void onHandleScoreboardObjective(S3BPacketScoreboardObjective packetIn, CallbackInfo ci) {
        if (this.clientWorldController == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleUpdateScore", at = @At("HEAD"), cancellable = true)
    private void onHandleUpdateScore(S3CPacketUpdateScore packetIn, CallbackInfo ci) {
        if (this.clientWorldController == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleDisplayScoreboard", at = @At("HEAD"), cancellable = true)
    private void onHandleDisplayScoreboard(S3DPacketDisplayScoreboard packetIn, CallbackInfo ci) {
        if (this.clientWorldController == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleTeams", at = @At("HEAD"), cancellable = true)
    private void onHandleTeams(S3EPacketTeams packetIn, CallbackInfo ci) {
        if (this.clientWorldController == null) {
            ci.cancel();
        }
    }

    @Inject(method = "handleWorldBorder", at = @At("HEAD"), cancellable = true)
    private void onHandleWorldBorder(S44PacketWorldBorder packetIn, CallbackInfo ci) {
        if (this.clientWorldController == null) {
            ci.cancel();
        }
    }
}