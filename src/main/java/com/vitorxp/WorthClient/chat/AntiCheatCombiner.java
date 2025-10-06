package com.vitorxp.WorthClient.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AntiCheatCombiner {

    private static final Pattern CHEAT_PATTERN = Pattern.compile(
            "(?i)Anti-?\\s*Cheat\\s*[➜→:>\\-]\\s*(.+?)\\s+falha\\s+em\\s+(.+?)" +
                    "(?:\\s*\\(\\s*Tipo\\s*([A-Za-z])\\s*\\))?" +
                    "(?:\\s*\\[(\\d+)\\/(\\d+)\\])?" +
                    "\\s*$"
    );

    private static final long COMBINE_TIME_MS = 10_000L;

    private static final Timer timer = new Timer("AntiCheatCombinerTimer", true);

    private static final class Entry {
        int count = 0;
        int lineId;
        String lastType = null;
        long lastUpdate = 0L;
    }

    private static final Map<String, Entry> entries = new HashMap<>();
    private static final Map<String, TimerTask> resetTasks = new HashMap<>();
    private static final AtomicInteger nextLineId = new AtomicInteger(2_200_000);

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();
        String clean = StringUtils.stripControlCodes(raw).trim();

        Matcher m = CHEAT_PATTERN.matcher(clean);
        if (!m.find()) {
            return;
        }

        event.setCanceled(true);

        String player = safe(m, 1);
        String cheatType = safe(m, 2);
        String typeLetter = safe(m, 3);
        String curStr = safe(m, 4);
        String maxStr = safe(m, 5);

        Integer cur = parseIntOrNull(curStr);
        Integer max = parseIntOrNull(maxStr);

        String key = (player + "|" + cheatType).toLowerCase();

        Entry e = entries.get(key);
        if (e == null) {
            e = new Entry();
            e.lineId = nextLineId.getAndIncrement();
            entries.put(key, e);
        }

        e.count++;
        e.lastUpdate = System.currentTimeMillis();
        if (!typeLetter.isEmpty()) e.lastType = typeLetter;

        StringBuilder sb = new StringBuilder();
        sb.append("§cAnti-Cheat ➜ §e").append(player)
                .append(" §7falha em §6").append(cheatType);
        if (e.lastType != null && !e.lastType.isEmpty()) {
            sb.append(" §7(§fTipo ").append(e.lastType).append("§7)");
        }
        sb.append(" §7[§a").append(e.count).append("§7]");
        if (cur != null && max != null) {
            sb.append(" §7[").append(cur).append("/").append(max).append("]");
        }

        ChatComponentText clickable = new ChatComponentText(sb.toString());
        ChatStyle style = new ChatStyle();
        style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vtp " + player));
        style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§eClique para teleportar em " + player)));
        clickable.setChatStyle(style);

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(
                clickable, e.lineId
        );


        TimerTask old = resetTasks.get(key);
        if (old != null) old.cancel();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Entry current = entries.get(key);
                if (current == null) return;

                if (now - current.lastUpdate >= COMBINE_TIME_MS) {
                    Minecraft mc = Minecraft.getMinecraft();
                    mc.addScheduledTask(() -> {
                        entries.remove(key);
                        resetTasks.remove(key);
                    });
                } else {
                    TimerTask again = this;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() { again.run(); }
                    }, COMBINE_TIME_MS);
                }
            }
        };
        resetTasks.put(key, task);
        timer.schedule(task, COMBINE_TIME_MS);
    }

    private static String safe(Matcher m, int group) {
        try {
            String g = m.group(group);
            return g == null ? "" : g.trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static Integer parseIntOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return null; }
    }
}