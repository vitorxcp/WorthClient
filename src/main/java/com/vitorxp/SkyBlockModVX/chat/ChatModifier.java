package com.vitorxp.SkyBlockModVX.chat;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.utils.RankUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
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

        if (formatted.contains("Você está invisível para os outros jogadores!") || formatted.length() < 10 || event.message.getChatStyle() == null) {
            return;
        }

        if (formatted.matches(".*[❤⚜✎❁✯⸕➣➤➥➫➳➵➸⏣⚔⛏].*")) {
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

        String remetente = extrairNome(raw);

        if (RankUtils.isStaff(Minecraft.getMinecraft().thePlayer)) {
            if (!remetente.equalsIgnoreCase("Desconhecido")) {
                IChatComponent adminButton = new ChatComponentText(" " + EnumChatFormatting.RED + "[❈]");
                adminButton.setChatStyle(new ChatStyle()
                        .setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/abrir_admin_gui " + remetente))
                        .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Abrir painel de administração")))
                );
                originalComponent.appendSibling(adminButton);
            }
        }


        Minecraft mc = Minecraft.getMinecraft();
        String playerName = mc.thePlayer.getName();

        if (raw.toLowerCase().contains(playerName.toLowerCase())) {
            mc.thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }

        event.setCanceled(true);
        mc.ingameGUI.getChatGUI().printChatMessage(originalComponent);
    }

    private String extrairNome(String raw) {
        int idx = raw.indexOf(":");
        if (idx == -1) return "Desconhecido";

        String parteAntesDoTexto = raw.substring(0, idx).trim();
        String[] partes = parteAntesDoTexto.split(" ");
        if (partes.length < 1) return "Desconhecido";

        String nomePossivel = partes[partes.length - 1];

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getNetHandler() != null) {
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                if (info.getGameProfile().getName().equalsIgnoreCase(nomePossivel)) {
                    return nomePossivel;
                }
            }
        }

        return "Desconhecido";
    }
}
