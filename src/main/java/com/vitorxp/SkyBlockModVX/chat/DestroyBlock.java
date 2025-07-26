package com.vitorxp.SkyBlockModVX.chat;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.MsgBlockDestroyBlock;

public class DestroyBlock {
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        if (!raw.contains(":")) {
            if (MsgBlockDestroyBlock && raw.equals("Hey! Você não pode quebrar blocos aqui.")) {
                event.setCanceled(true);
            }
        }
    }
}
