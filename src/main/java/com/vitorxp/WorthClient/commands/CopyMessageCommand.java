package com.vitorxp.WorthClient.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CopyMessageCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "copiar_mensagem";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/copiar_mensagem <mensagem>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) return;

        String msg = String.join(" ", args);
        GuiScreen.setClipboardString(msg);

        Minecraft.getMinecraft().thePlayer.addChatMessage(
                new ChatComponentText(EnumChatFormatting.GREEN + "Mensagem copiada para a área de transferência!")
        );
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
