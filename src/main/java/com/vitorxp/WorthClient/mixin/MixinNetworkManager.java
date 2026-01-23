package com.vitorxp.WorthClient.mixin;

import com.viaversion.viaversion.api.connection.UserConnection;
import de.florianmichael.viamcp.ViaMCP;
import de.florianmichael.viamcp.MCPVLBPipeline;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "channelActive", at = @At("HEAD"))
    public void onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
        Channel channel = context.channel();

        if (channel instanceof SocketChannel && ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaMCP.NATIVE_VERSION) {

            final UserConnection user = new UserConnectionImpl(channel, true);
            new ProtocolPipelineImpl(user);

            context.pipeline().addLast(new MCPVLBPipeline(user));
        }
    }

    @Inject(method = "setCompressionTreshold", at = @At("HEAD"), cancellable = true)
    public void onSetCompressionTreshold(int treshold, CallbackInfo ci) {
        if (ViaLoadingBase.getInstance().getTargetVersion().getVersion() > 47) {
            ci.cancel();
        }
    }
}