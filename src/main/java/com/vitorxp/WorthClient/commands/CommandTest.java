package com.vitorxp.WorthClient.commands;

import com.vitorxp.WorthClient.utils.RankUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandTest implements ICommand {

    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/test";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        boolean staff = RankUtils.isStaff(player);

        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(staff ? "Você é da staff, boa!" : "Que pena, você não é da staff."));
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}
