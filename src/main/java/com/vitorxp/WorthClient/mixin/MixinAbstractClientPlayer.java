package com.vitorxp.WorthClient.mixin;

import com.mojang.authlib.GameProfile;
import com.vitorxp.WorthClient.socket.ClientSocket;
import com.vitorxp.WorthClient.utils.AnimatedCape;
import com.vitorxp.WorthClient.utils.CapeLoader;
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
import java.util.Set;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends EntityPlayer {

    public MixinAbstractClientPlayer(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
    }
    @Shadow @Nullable protected abstract NetworkPlayerInfo getPlayerInfo();
    @Inject(method = "getLocationCape", at = @At("HEAD"), cancellable = true)
    public void getLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        if (this.getGameProfile() == null || this.getGameProfile().getName() == null) return;
        String playerName = this.getGameProfile().getName().toLowerCase();
        if (ClientSocket.playerCosmetics.containsKey(playerName)) {
            Set<String> cosmetics = ClientSocket.playerCosmetics.get(playerName);

            for (String cosmeticId : cosmetics) {
                AnimatedCape animCape = CapeLoader.getAnimatedCape(cosmeticId);
                if (animCape != null) {
                    animCape.update();
                    cir.setReturnValue(animCape.getTextureLocation());
                    return;
                }
                ResourceLocation staticCape = CapeLoader.loadCape(cosmeticId);
                if (staticCape != null) {
                    cir.setReturnValue(staticCape);
                    return;
                }
            }
        }
    }
}