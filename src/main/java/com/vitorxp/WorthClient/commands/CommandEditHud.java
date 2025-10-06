package com.vitorxp.WorthClient.commands;

import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Collections;
import java.util.List;

public class CommandEditHud implements ICommand {

    @Override
    public String getCommandName() {
        return "edithud";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/edithud";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        com.vitorxp.WorthClient.WorthClient.pendingOpenMenuHud = true;
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}