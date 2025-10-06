package com.vitorxp.WorthClient.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandPetMaxBlock implements ICommand {

    @Override
    public String getCommandName() {
        return "petmaxblock";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/petmaxblock";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        com.vitorxp.WorthClient.WorthClient.blockPetMessages = !com.vitorxp.WorthClient.WorthClient.blockPetMessages;
        String status = com.vitorxp.WorthClient.WorthClient.blockPetMessages ? "§aativado" : "§cdesativado";
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§bPetChat §8➜ §eBloqueio pet nível máximo " + status + "§e!")
        );
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}
