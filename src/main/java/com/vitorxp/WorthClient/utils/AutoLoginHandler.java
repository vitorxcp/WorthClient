package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.manager.AutoLoginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class AutoLoginHandler {

    private boolean pendingLogin = false;
    private int delayTicks = 0;
    private String commandToExecute = null;

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        ServerData serverData = mc.getCurrentServerData();

        if (serverData != null) {
            AutoLoginManager.ServerConfig config = AutoLoginManager.getServerConfig(serverData.serverIP);

            if (config != null) {
                String currentNick = mc.getSession().getUsername();
                String password = config.getPassword(currentNick);

                if (password != null) {
                    commandToExecute = config.loginCommand.replace("{password}", password);
                    pendingLogin = true;
                    delayTicks = 0;
                }
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        if (pendingLogin) {
            delayTicks++;

            if (delayTicks > 40) {
                if (Minecraft.getMinecraft().thePlayer != null) {
                    if(!WorthClient.AutoLoginEnabled) return;

                    Minecraft.getMinecraft().thePlayer.sendChatMessage(commandToExecute);
                    Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
                            new net.minecraft.util.ChatComponentText("§a[WorthClient] §fLogin automático executado!")
                    );
                }
                pendingLogin = false;
                commandToExecute = null;
                delayTicks = 0;
            }
        }
    }
}