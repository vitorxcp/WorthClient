package com.vitorxp.WorthClient.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

import static com.vitorxp.WorthClient.WorthClient.pendingPlayersTP;

public class ECTPCOmmand extends CommandBase {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public ECTPCOmmand() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public String getCommandName() {
        return "vtp";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/vtp <nick>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText("§cUso correto: /vtp <nick>"));
            return;
        }

        String target = args[0];
        String key = target.toLowerCase();

        EntityPlayerSP player = mc.thePlayer;
        if (player == null) {
            sender.addChatMessage(new ChatComponentText("§cErro: jogador não disponível."));
            return;
        }

        pendingPlayersTP.put(key, target);

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
            pendingPlayersTP.forEach((k, v) -> {
                mc.addScheduledTask(() ->
                        mc.thePlayer.addChatMessage(new ChatComponentText("§cServidor negou permissão para comando ao tentar teleportar " + v))
                );
            });
            pendingPlayersTP.clear();
            return;
        }

        for (Map.Entry<String, String> entry : pendingPlayersTP.entrySet().toArray(new Map.Entry[0])) {
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

                pendingPlayersTP.remove(key);
            }
        }
    }

    @Override
    public java.util.List<String> addTabCompletionOptions(ICommandSender sender, String[] args, net.minecraft.util.BlockPos pos) {
        if (args.length == 1) {
            return mc.getNetHandler().getPlayerInfoMap().stream()
                    .map(info -> info.getGameProfile().getName())
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return java.util.Collections.emptyList();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}