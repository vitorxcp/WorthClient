package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class AdminGui extends GuiScreen {

    private final String targetName;

    public AdminGui(String playerName) {
        this.targetName = playerName;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        this.buttonList.add(new GuiButton(1, width / 2 - 100, height / 2 - 30, "Ver Histórico"));
        this.buttonList.add(new GuiButton(2, width / 2 - 100, height / 2, "Aplicar Aviso"));
        this.buttonList.add(new GuiButton(3, width / 2 - 100, height / 2 + 30, "Punir Jogador"));
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
