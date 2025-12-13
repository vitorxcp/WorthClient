package com.vitorxp.WorthClient.mixin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannelConfig;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelActive", at = @At("HEAD"))
    public void onChannelActive(ChannelHandlerContext ctx, CallbackInfo ci) {
        try {
            if (ctx.channel().config() instanceof SocketChannelConfig) {
                SocketChannelConfig config = (SocketChannelConfig) ctx.channel().config();

                config.setTcpNoDelay(true);

                config.setTrafficClass(0x18);

                config.setKeepAlive(true);
            }
        } catch (Exception ignored) {
        }
    }
}