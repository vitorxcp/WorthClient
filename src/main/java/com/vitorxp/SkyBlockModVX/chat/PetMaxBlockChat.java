package com.vitorxp.SkyBlockModVX.chat;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.blockPetMessages;

public class PetMaxBlockChat {
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        if (!raw.contains(":")) {
            if (blockPetMessages && raw.equals("Seu pet está nível máximo!")) {
                event.setCanceled(true);
            }
        }
    }
}
