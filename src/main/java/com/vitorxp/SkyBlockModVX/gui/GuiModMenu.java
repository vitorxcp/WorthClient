package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.manager.ActivationManager;
import com.vitorxp.SkyBlockModVX.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiModMenu extends GuiScreen {

    private GuiButton btnPet, btnMutante, btnOvewrlays, btnChatC, btnPerspective;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;

    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnPet = new GuiButton(1, centerX - 125, y, 250, 20,
                "§9[Config] §7Configurações de Pets");
        this.buttonList.add(btnPet); y += 25;

        btnChatC = new GuiButton(12, centerX - 125, y, 250, 20,
                "§9[Config] §7Configurações do Chat");
        this.buttonList.add(btnChatC); y += 25;

        btnOvewrlays = new GuiButton(13, centerX - 125, y, 250, 20,
                "§9[Config] §7Configurações de Overlays");

        this.buttonList.add(btnOvewrlays); y += 25;

        btnPerspective = new GuiButton(14, centerX - 125, y, 250, 20,
                "§9[Config] §7Configurações do Perspective Mod");

        this.buttonList.add(btnPerspective); y += 25;

        btnMutante = new GuiButton(4, centerX - 125, y, 250, 20,
                "§6[Alerta] §7Mutante: " + getStatus(SkyBlockMod.announceZealot));
        if (!ActivationManager.isActivated) {
            btnMutante.enabled = false;
            btnMutante.displayString = "§c[Mutante] Alerta (bloqueado)";
        }
        this.buttonList.add(btnMutante); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            mc.displayGuiScreen(null);
            SkyBlockMod.guiEditorPet = true;
        } else if (button.id == 4 && ActivationManager.isActivated) {
            SkyBlockMod.announceZealot = !SkyBlockMod.announceZealot;
            button.displayString = "§d[Mutante] §7Alerta: " + getStatus(SkyBlockMod.announceZealot);
        } else if (button.id == 12) {
            mc.displayGuiScreen(null);
            SkyBlockMod.guiEditorChat = true;
        } else if (button.id == 13) {
            mc.displayGuiScreen(null);
            SkyBlockMod.GuiOverlay = true;
        } else if (button.id == 14) {
            mc.displayGuiScreen(null);
            SkyBlockMod.GuiPerspective = true;
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

        this.drawCenteredString(this.fontRendererObj, "§6WorthMod §f– §7Configurações", this.width / 2, 20, (buttonAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
