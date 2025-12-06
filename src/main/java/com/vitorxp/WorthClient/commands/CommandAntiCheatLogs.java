package com.vitorxp.WorthClient.commands;

import com.vitorxp.WorthClient.chat.AntiCheatCombiner;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.LinkedList;
import java.util.Map;

public class CommandAntiCheatLogs extends CommandBase {

    @Override
    public String getCommandName() {
        return "anticheatlogs";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/anticheatlogs [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("§6§lLogs do AntiCheat (Sessão Atual):"));

            if (AntiCheatCombiner.history.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("§cNenhum log registrado ainda."));
                return;
            }

            for (String playerName : AntiCheatCombiner.history.keySet()) {
                ChatComponentText text = new ChatComponentText(" §7- §e" + playerName);

                ChatStyle style = new ChatStyle();
                style.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/anticheatlogs " + playerName));
                style.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ChatComponentText("§aClique para ver as anomalias de " + playerName)));
                text.setChatStyle(style);

                sender.addChatMessage(text);
            }
            sender.addChatMessage(new ChatComponentText("§7Clique em um nome para ver os detalhes."));
        }
        else {
            String target = args[0].toLowerCase();
            LinkedList<AntiCheatCombiner.StoredLog> logs = AntiCheatCombiner.history.get(target);

            if (logs == null || logs.isEmpty()) {
                sender.addChatMessage(new ChatComponentText("§cNenhum log encontrado para o jogador: " + args[0]));
                return;
            }

            sender.addChatMessage(new ChatComponentText("§6§lAnomalias de " + logs.getFirst().playerName + ":"));

            for (AntiCheatCombiner.StoredLog log : logs) {
                IChatComponent msg = AntiCheatCombiner.buildMessage(
                        log.playerName,
                        log.cheatType,
                        log.typeLetter,
                        log.count,
                        log.currentVl,
                        log.maxVl
                );
                sender.addChatMessage(msg);
            }
        }
    }
}