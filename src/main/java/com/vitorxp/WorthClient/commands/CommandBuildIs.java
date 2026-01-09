package com.vitorxp.WorthClient.commands;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import static com.vitorxp.WorthClient.utils.RankUtils.isStaffM;

public class CommandBuildIs extends CommandBase {

    @Override
    public String getCommandName() {
        return "buildis";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/buildis";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (!isStaffM(Minecraft.getMinecraft().thePlayer)) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Apenas Staff pode usar este comando."));
            return;
        }

        WorthClient.buildEnabled = !WorthClient.buildEnabled;

        if (WorthClient.buildEnabled) {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Modo Construção: ATIVADO (Cuidado!)"));
        } else {
            sender.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "Modo Construção: DESATIVADO (Protegido)"));
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}