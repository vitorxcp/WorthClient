package com.vitorxp.SkyBlockModVX.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ECTPCOmmand extends CommandBase {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final Map<String, String> pending = new ConcurrentHashMap<>();

    public ECTPCOmmand() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getCommandName() {
        return "ac_tp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/ac_tp <nick>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("§cUso correto: /ac_tp <nick>"));
            return;
        }

        String target = args[0];
        String key = target.toLowerCase();

        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            sender.addChatMessage(new ChatComponentText("§cErro: jogador não disponível."));
            return;
        }

        pending.put(key, target);

        mc.addScheduledTask(() -> {
            player.sendChatMessage("/v");
        });
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();
        String clean = StringUtils.stripControlCodes(raw).toLowerCase();

        if (clean.isEmpty()) return;

        if (clean.contains("you do not have permission") || clean.contains("sem permissão") || clean.contains("não tem permissão")) {
            pending.forEach((k, v) -> {
                mc.addScheduledTask(() ->
                        mc.thePlayer.addChatMessage(new ChatComponentText("§c/ac_tp: servidor negou permissão para comando ao tentar teleportar " + v))
                );
            });
            pending.clear();
            return;
        }

        for (Map.Entry<String, String> entry : pending.entrySet().toArray(new Map.Entry[0])) {
            String key = entry.getKey();
            String nick = entry.getValue();

            boolean invisActivated = false;
            boolean invisDeactivated = false;

            if (clean.contains("invisível") || clean.contains("invisible") || clean.contains("invisivel")) {
                if (clean.contains("ativ") || clean.contains("activated") || clean.contains("on")) invisActivated = true;
                if (clean.contains("desativ") || clean.contains("deactivated") || clean.contains("off")) invisDeactivated = true;

                if (clean.contains("completamente invisível") || clean.contains("you are now completely invisible")) invisActivated = true;
            }

            if (invisActivated || invisDeactivated) {
                final String targetNick = nick;
                final boolean wasDeactivated = invisDeactivated;

                mc.addScheduledTask(() -> {
                    EntityPlayerSP p = mc.thePlayer;
                    if (p == null) return;

                    if (wasDeactivated) {
                        p.sendChatMessage("/v");
                        mc.addScheduledTask(() -> {
                            p.sendChatMessage("/stp " + targetNick);
                            p.sendChatMessage("/tp " + targetNick);
                        });
                    } else {
                        p.sendChatMessage("/stp " + targetNick);
                        p.sendChatMessage("/tp " + targetNick);
                    }
                });

                pending.remove(key);
            }
        }
    }

    @Override
    public java.util.List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        if (args.length == 1) {
            return mc.getNetHandler().getPlayerInfoMap().stream()
                    .map(info -> info.getGameProfile().getName())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}