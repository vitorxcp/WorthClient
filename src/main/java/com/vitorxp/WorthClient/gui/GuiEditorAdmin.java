package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiEditorAdmin extends GuiScreen {

    private GuiButton btnRadar, btnReturn;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnRadar = new GuiButton(2, centerX - 125, y, 250, 20,
                "§6[Radar] §7Mostrar Radar: " + getStatus(com.vitorxp.WorthClient.WorthClient.RadarOverlay));

        this.buttonList.add(btnRadar); y += 25;

        btnReturn = new GuiButton(10, centerX - 125, y, 250, 20,
                "Voltar");
        this.buttonList.add(btnReturn); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            com.vitorxp.WorthClient.WorthClient.RadarOverlay = !com.vitorxp.WorthClient.WorthClient.RadarOverlay;
            button.displayString = "§6[Radar] §7Mostrar Radar: " + getStatus(com.vitorxp.WorthClient.WorthClient.RadarOverlay);
        }  else if (button.id == 10) {
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
