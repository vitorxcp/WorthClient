package com.vitorxp.SkyBlockModVX.commands;

import com.vitorxp.SkyBlockModVX.gui.AdminGui;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.Collections;
import java.util.List;

public class AdminCommandStaff implements ICommand {
    @Override
    public String getCommandName() {
        return "abrir_admin_gui";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/abrir_admin_gui <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length >= 1) {
            Minecraft.getMinecraft().displayGuiScreen(new AdminGui(args[0]));
        }
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}
