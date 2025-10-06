package com.vitorxp.WorthClient.commands;

import com.vitorxp.WorthClient.gui.AdminGui;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

import static com.vitorxp.WorthClient.utils.RankUtils.isStaff;

public class AdminCommandStaff extends CommandBase {
    @Override
    public String getCommandName() {
        return "adminv";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/adminv <player>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!isStaff(Minecraft.getMinecraft().thePlayer)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cVocê não faz parte da Staff para executar esse comando!"));
            return;
        }
        if (args.length >= 1) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            Minecraft.getMinecraft().displayGuiScreen(new AdminGui(args[0]));

            com.vitorxp.WorthClient.WorthClient.nameArsAdmin = args[0];
            com.vitorxp.WorthClient.WorthClient.GuiAdminazw = true;

            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§aPainel Administrativo aberto!"));
        }
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
    @Override public int getRequiredPermissionLevel() {
        return 0;
    }
}
