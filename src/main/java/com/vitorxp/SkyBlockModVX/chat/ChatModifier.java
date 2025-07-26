package com.vitorxp.SkyBlockModVX.chat;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatModifier {

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String formatted = event.message.getFormattedText();
        String raw = event.message.getUnformattedText();

        if ((formatted.contains("❤") && formatted.contains("Defesa") && formatted.contains("Mana")) ||
                formatted.matches(".*[❤⚜✎❁✯⸕➣➤➥➫➳➵➸⏣⚔⛏].*") ||
                !formatted.contains(":") ||
                formatted.length() < 10 ||
                event.message.getChatStyle() == null) {
            return;
        }

        IChatComponent originalComponent = event.message.createCopy();

        if (SkyBlockMod.showTime) {
            String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
            IChatComponent timePrefix = new ChatComponentText(EnumChatFormatting.GRAY + "[" + hora + "] " + EnumChatFormatting.RESET);
            timePrefix.appendSibling(originalComponent);
            originalComponent = timePrefix;
        }

        IChatComponent copyButton = new ChatComponentText(" " + EnumChatFormatting.DARK_AQUA + "[⎆]");
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String mensagemCompleta = "[" + hora + "] " + raw;

        copyButton.setChatStyle(new ChatStyle()
                .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/copiar_mensagem \"" + mensagemCompleta.replaceAll("\"", "'") + "\""))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Clique aqui para copiar esta mensagem.")))
        );

        if (SkyBlockMod.enableCopy) originalComponent.appendSibling(copyButton);

        Minecraft mc = Minecraft.getMinecraft();
        String playerName = mc.thePlayer.getName();

        if (raw.toLowerCase().contains(playerName.toLowerCase())) {
            mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }

        event.setCanceled(true);
        mc.ingameGUI.getChatGUI().printChatMessage(originalComponent);
    }
}
