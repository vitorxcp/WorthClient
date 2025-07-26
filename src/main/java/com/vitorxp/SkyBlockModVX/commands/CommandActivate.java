package com.vitorxp.SkyBlockModVX.commands;

import com.vitorxp.SkyBlockModVX.manager.ActivationManager;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class CommandActivate implements ICommand {

    @Override
    public String getCommandName() {
        return "activated";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/activated <chave>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(
                    new ChatComponentText("§cUso correto: /activated <chave>")
            );
            return;
        }

        String key = args[0];
        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText("§eVerificando chave...")
        );

        ActivationManager.attemptActivation(key);
    }

    @Override public int compareTo(ICommand o) { return getCommandName().compareTo(o.getCommandName()); }
    @Override public List<String> getCommandAliases() { return Collections.emptyList(); }
    @Override public boolean canCommandSenderUseCommand(ICommandSender sender) { return true; }
    @Override public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) { return Collections.emptyList(); }
    @Override public boolean isUsernameIndex(String[] args, int index) { return false; }
}
