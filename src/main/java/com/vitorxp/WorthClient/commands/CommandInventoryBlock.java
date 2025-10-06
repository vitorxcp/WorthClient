package com.vitorxp.WorthClient.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandInventoryBlock implements ICommand {

    @Override
    public String getCommandName() {
        return "inventoryblock";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/inventoryblock";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        com.vitorxp.WorthClient.WorthClient.blockInventoryMessages = !com.vitorxp.WorthClient.WorthClient.blockInventoryMessages;
        String status = com.vitorxp.WorthClient.WorthClient.blockInventoryMessages ? "§aativado" : "§cdesativado";
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§bInventoryChat §8➜ §eBloqueio inventário cheio " + status + "§e!")
        );
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}