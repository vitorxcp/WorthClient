package com.vitorxp.WorthClient.chat;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatCommandTracker {
    private static String lastCommand = "";
    private static long lastCommandTime = 0;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String msg = event.message.getUnformattedText();
        if(msg.startsWith("/")) {
            lastCommand = msg;
            lastCommandTime = System.currentTimeMillis();
        }
    }

    public static String getLastCommand() {
        return (System.currentTimeMillis() - lastCommandTime < 3000) ? lastCommand : "Desconhecido";
    }
}
