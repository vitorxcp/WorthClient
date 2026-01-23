package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.Logger;
import net.minecraft.network.EnumConnectionState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting extends GuiScreen {

    @Shadow @Final private static Logger logger;
    @Shadow private NetworkManager networkManager;
    @Shadow private boolean cancel;
    @Shadow @Final private GuiScreen previousGuiScreen;
    @Shadow @Final private static AtomicInteger CONNECTION_ID;

    /**
     * @author WorthClient
     * @reason Proteção contra Crash do Netty/ViaMCP
     */
    @Overwrite
    private void connect(final String ip, final int port) {
        logger.info("Connecting to " + ip + ", " + port);

        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            public void run() {
                InetAddress inetaddress = null;

                try {
                    if (MixinGuiConnecting.this.cancel) {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);

                    MixinGuiConnecting.this.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());

                    MixinGuiConnecting.this.networkManager.setNetHandler(new NetHandlerLoginClient(MixinGuiConnecting.this.networkManager, mc, MixinGuiConnecting.this.previousGuiScreen));
                    MixinGuiConnecting.this.networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));
                    MixinGuiConnecting.this.networkManager.sendPacket(new C00PacketLoginStart(mc.getSession().getProfile()));

                } catch (UnknownHostException e) {
                    if (MixinGuiConnecting.this.cancel) return;
                    logger.error("Couldn't connect to server", e);
                    mc.displayGuiScreen(new GuiDisconnected(MixinGuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown Host: " + e.getMessage())));

                } catch (Exception exception) {
                    if (MixinGuiConnecting.this.cancel) return;

                    logger.error("Couldn't connect to server", exception);
                    String erro = exception.toString();

                    if(erro.contains("AbstractBootstrap.group")) {
                        erro = "Erro Crítico: ViaMCP não iniciou ou Netty falhou.";
                    }

                    mc.displayGuiScreen(new GuiDisconnected(MixinGuiConnecting.this.previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "WorthClient Protection: " + erro)));
                }
            }
        }).start();
    }
}