package com.vitorxp.SkyBlockModVX.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SellMessageCombiner {

    private static final Pattern SELL_PATTERN = Pattern.compile("Vendido com sucesso por ([\\d,.]+) coins!?");
    private Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    private static double totalSold = 0.0;
    private static long lastSellTime = 0;
    private static final long COMBINE_TIME_MS = 10000;

    private static final Timer timer = new Timer();
    private static TimerTask resetTask = null;

    private static int lastChatLineId = 1000000;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        Matcher matcher = SELL_PATTERN.matcher(raw);
        if (matcher.matches()) {
            event.setCanceled(true);

            String valueText = matcher.group(1).replaceAll("[^\\d]", "");
            double value = Double.parseDouble(valueText);

            totalSold += value;
            lastSellTime = System.currentTimeMillis();

            String formatted = String.format("%,.2f", totalSold);

            getMc().ingameGUI.getChatGUI().deleteChatLine(lastChatLineId);

            String msg = "§aTotal vendido/comprado: §6" + formatted + " coins";
            ChatComponentText newMsg = new ChatComponentText("§6[WorthMod] §r" + msg);

            getMc().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(newMsg, lastChatLineId);

            if (resetTask != null) resetTask.cancel();
            resetTask = new TimerTask() {
                @Override
                public void run() {
                    totalSold = 0;
                }
            };
            timer.schedule(resetTask, COMBINE_TIME_MS);
        }
    }
}