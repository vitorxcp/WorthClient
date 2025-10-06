package com.vitorxp.WorthClient.chat;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.vitorxp.WorthClient.WorthClient.blockInventoryMessages;

public class InventoryFullBlock {
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        if (!raw.contains(":")) {
            if (blockInventoryMessages && raw.equals("Seu inventário está cheio!")) {
                event.setCanceled(true);
            }
        }
    }
}
