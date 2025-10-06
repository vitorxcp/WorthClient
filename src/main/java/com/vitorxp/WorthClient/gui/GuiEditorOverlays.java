package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.manager.ActivationManager;
import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiEditorOverlays extends GuiScreen {

    private GuiButton btnPetOverlay, btnPingOverlay, btnFPSOverlay, btnViewKeyBoard, btnEditorKey, btnArmorOverlay, btnReturn;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnPetOverlay = new GuiButton(5, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Pet HUD: " + getStatus(com.vitorxp.WorthClient.WorthClient.petOverlay));

        if (!ActivationManager.isActivated) {
            btnPetOverlay.enabled = false;
            btnPetOverlay.displayString = "§c[Overlay] Pet HUD (bloqueado)";
        }

        this.buttonList.add(btnPetOverlay); y += 25;

        btnPingOverlay = new GuiButton(6, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Ping: " + getStatus(com.vitorxp.WorthClient.WorthClient.pingOverlay));
        this.buttonList.add(btnPingOverlay); y += 25;

        btnFPSOverlay = new GuiButton(7, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar FPS: " + getStatus(com.vitorxp.WorthClient.WorthClient.fpsOverlay));
        this.buttonList.add(btnFPSOverlay); y += 25;

        btnViewKeyBoard = new GuiButton(9, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar KeyBoard: " + getStatus(com.vitorxp.WorthClient.WorthClient.keystrokesOverlay));
        this.buttonList.add(btnViewKeyBoard); y += 25;

        btnEditorKey = new GuiButton(13, centerX - 125, y, 250, 20,
                "§9[Keystrokes] §7Configurações");
        this.buttonList.add(btnEditorKey); y += 25;

        btnArmorOverlay = new GuiButton(8, centerX - 125, y, 250, 20,
                "§9[Armaduras] §7Configurações");
        this.buttonList.add(btnArmorOverlay); y += 25;

        btnReturn = new GuiButton(10, centerX - 125, y, 250, 20,
                "Voltar");
        this.buttonList.add(btnReturn); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 5 && ActivationManager.isActivated) {
            com.vitorxp.WorthClient.WorthClient.petOverlay = !com.vitorxp.WorthClient.WorthClient.petOverlay;
            button.displayString = "§b[Overlay] §7Pet HUD: " + getStatus(com.vitorxp.WorthClient.WorthClient.petOverlay);
        } else if (button.id == 6) {
            com.vitorxp.WorthClient.WorthClient.pingOverlay = !com.vitorxp.WorthClient.WorthClient.pingOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Ping: " + getStatus(com.vitorxp.WorthClient.WorthClient.pingOverlay);
        } else if (button.id == 7) {
            com.vitorxp.WorthClient.WorthClient.fpsOverlay = !com.vitorxp.WorthClient.WorthClient.fpsOverlay;
            button.displayString = "§b[Overlay] §7Mostrar FPS: " + getStatus(com.vitorxp.WorthClient.WorthClient.fpsOverlay);
        } else if (button.id == 9) {
            com.vitorxp.WorthClient.WorthClient.keystrokesOverlay = !com.vitorxp.WorthClient.WorthClient.keystrokesOverlay;
            button.displayString = "§b[Overlay] §7Mostrar KeyBoard: " + getStatus(com.vitorxp.WorthClient.WorthClient.keystrokesOverlay);
        } else if (button.id == 8) {
            mc.displayGuiScreen(null);
            com.vitorxp.WorthClient.WorthClient.guiEditorArmor = true;
        } else if (button.id == 13) {
            mc.displayGuiScreen(null);
            com.vitorxp.WorthClient.WorthClient.GuiKeyEditor = true;
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

        this.drawCenteredString(this.fontRendererObj, "§6WorthClient §f– §7Editor de Overlays", this.width / 2, 20, (buttonAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
