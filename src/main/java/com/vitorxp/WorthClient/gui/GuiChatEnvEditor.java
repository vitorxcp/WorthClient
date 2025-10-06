package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiChatEnvEditor extends GuiScreen {

    private GuiButton btnInventory, btnDestroyBlock, btnPetBlock, btnDateView, btnCopyMessage, btnReturn;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnPetBlock = new GuiButton(1, centerX - 150, y, 300, 20,
                "§3[Chat] §7Desativar mensagem de pet maxímo: " + getStatus(com.vitorxp.WorthClient.WorthClient.petOverlay));
        this.buttonList.add(btnPetBlock); y += 25;

        btnInventory = new GuiButton(2, centerX - 150, y, 300, 20,
                "§3[Chat] §7Desativar mensagem de inventário: " + getStatus(com.vitorxp.WorthClient.WorthClient.blockInventoryMessages));
        this.buttonList.add(btnInventory); y += 25;

        btnDestroyBlock = new GuiButton(3, centerX - 150, y, 300, 20,
                "§3[Chat] §7Desativar aviso ao quebrar blocos: " + getStatus(com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock));
        this.buttonList.add(btnDestroyBlock); y += 25;

        btnCopyMessage = new GuiButton(4, centerX - 150, y, 300, 20,
                "§3[Chat] §7Botão para copiar mensagem: " + getStatus(com.vitorxp.WorthClient.WorthClient.enableCopy));
        this.buttonList.add(btnCopyMessage); y += 25;

        btnDateView = new GuiButton(5, centerX - 150, y, 300, 20,
                "§3[Chat] §7Mostrar data de envio: " + getStatus(com.vitorxp.WorthClient.WorthClient.showTime));
        this.buttonList.add(btnDateView); y += 25;

        btnReturn = new GuiButton(10, centerX - 150, y, 300, 20,
                "Voltar");
        this.buttonList.add(btnReturn); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            com.vitorxp.WorthClient.WorthClient.petOverlay = !com.vitorxp.WorthClient.WorthClient.petOverlay;
            button.displayString = "§3[Chat] §7Desativar mensagem de pet maxímo: " + getStatus(com.vitorxp.WorthClient.WorthClient.petOverlay);
        } else if (button.id == 2) {
            com.vitorxp.WorthClient.WorthClient.blockInventoryMessages = !com.vitorxp.WorthClient.WorthClient.blockInventoryMessages;
            button.displayString = "§3[Chat] §7Desativar mensagem de Inventário: " + getStatus(com.vitorxp.WorthClient.WorthClient.blockInventoryMessages);
        } else if (button.id == 3) {
            com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock = !com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock;
            button.displayString = "§3[Chat] §7Desativar aviso ao quebrar blocos: " + getStatus(com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock);
        } else if (button.id == 4) {
            com.vitorxp.WorthClient.WorthClient.enableCopy = !com.vitorxp.WorthClient.WorthClient.enableCopy;
            button.displayString = "§3[Chat] §7Botão para copiar mensagem: " + getStatus(com.vitorxp.WorthClient.WorthClient.enableCopy);
        } else if (button.id == 5) {
            com.vitorxp.WorthClient.WorthClient.showTime = !com.vitorxp.WorthClient.WorthClient.showTime;
            button.displayString = "§3[Chat] §7Mostrar data de envio: " + getStatus(com.vitorxp.WorthClient.WorthClient.showTime);
        } else if (button.id == 10) {
            mc.displayGuiScreen(null);
            com.vitorxp.WorthClient.WorthClient.pendingOpenMenu = true;
        }

        ConfigManager.save();
    }

    private String getStatus(boolean value) {
        return value ? "§aAtivado" : "§cDesativado";
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (fadeIn && alpha < 1f) {
            alpha += 0.05f;
            if (alpha > 1f) alpha = 1f;
        } else if (!fadeIn && alpha > 0f) {
            alpha -= 0.05f;
            if (alpha < 0f) alpha = 0f;
        }

        int bgColor = ((int)(0.3f * 255) << 24) | 0x000000;

        this.drawRect(0, 0, this.width, this.height, bgColor);

        buttonAlpha = (int)(alpha * 255);

        this.drawCenteredString(this.fontRendererObj, "§6WorthClient §f– §7Editor de Pet", this.width / 2, 20, (buttonAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
