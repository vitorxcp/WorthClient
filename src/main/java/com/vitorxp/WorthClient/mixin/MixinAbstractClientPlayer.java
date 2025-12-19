package com.vitorxp.WorthClient.mixin;

import com.mojang.authlib.GameProfile;
import com.vitorxp.WorthClient.socket.ClientSocket;
import com.vitorxp.WorthClient.utils.CapeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {

    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }

    @Shadow @Nullable protected abstract NetworkPlayerInfo getPlayerInfo();

    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.getGameProfile() == null || this.getGameProfile().getName() == null) return;

        String playerName = this.getGameProfile().getName();

        if (ClientSocket.hasCosmetic(playerName, "cape_free_redeworth")) {

            ResourceLocation worth_cape_free = CapeLoader.loadCape("cape_free_redeworth");

            if (worth_cape_free != null) {
                cir.setReturnValue(worth_cape_free);
            }
        }
    }
}