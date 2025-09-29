package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.pendingPlayersTP;
import static com.vitorxp.SkyBlockModVX.utils.RankUtils.isStaffM;

public class AdminGui extends GuiScreen {

    private final String targetName;

    public AdminGui(String playerName) {
        this.targetName = playerName;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, width / 2 - 100, height / 2 - 30, "§eVer Histórico"));
        this.buttonList.add(new GuiButton(2, width / 2 - 100, height / 2, "§cAplicar Aviso"));
        this.buttonList.add(new GuiButton(3, width / 2 - 100, height / 2 + 30, "§4Punir Jogador"));
        this.buttonList.add(new GuiButton(4, width / 2 - 100, height / 2 + 60, "§3Dar TP no Jogador (normal)"));
        this.buttonList.add(new GuiButton(5, width / 2 - 100, height / 2 + 90, "§3Dar TP no Jogador (invisível)"));
        if (isStaffM(mc.thePlayer)) this.buttonList.add(new GuiButton(6, width / 2 - 100, height / 2 + 120, "§4nLogin"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 1:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/historico " + targetName);
                break;
            case 2:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/warn " + targetName);
                break;
            case 3:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/punir " + targetName);
                break;
            case 4:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/tp " + targetName);
                break;
            case 5:
                String target = targetName;
                String key = target.toLowerCase();

                EntityPlayerSP player = mc.thePlayer;
                if (player == null) {
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cErro: jogador não disponível."));
                    return;
                }

                pendingPlayersTP.put(key, target);

                mc.addScheduledTask(() -> {
                    player.sendChatMessage("/v");
                });
                break;
            case 6:
                Minecraft.getMinecraft().thePlayer.sendChatMessage("/nlogin verify " + targetName);
                break;
        }
        this.mc.displayGuiScreen(null);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Painel de Administração: " + targetName, width / 2, height / 2 - 60, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
