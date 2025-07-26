package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.manager.ActivationManager;
import com.vitorxp.SkyBlockModVX.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiEditorPet extends GuiScreen {

    private GuiButton btnViewsPets, btnPetsAllRemove, btnReturn;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnViewsPets = new GuiButton(2, centerX - 125, y, 250, 20,
                "§e[Pet] §7Visibilidade de Pet: " + getStatus(SkyBlockMod.petDisplayViewOff));

        if (!ActivationManager.isActivated) {
            btnViewsPets.enabled = false;
            btnViewsPets.displayString = "§c[Pet] Visibilidade (bloqueado)";
        }
        this.buttonList.add(btnViewsPets); y += 25;

        btnPetsAllRemove = new GuiButton(3, centerX - 125, y, 250, 20,
                "§e[Pet] §7Remover todos os Pets: " + getStatus(SkyBlockMod.viewsPetAll));

        if (!ActivationManager.isActivated) {
            btnPetsAllRemove.enabled = false;
            btnPetsAllRemove.displayString = "§c[Pet] Remover Pets (bloqueado)";
        }
        this.buttonList.add(btnPetsAllRemove); y += 25;


        btnReturn = new GuiButton(10, centerX - 125, y, 250, 20,
                "Voltar");
        this.buttonList.add(btnReturn); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 2) {
            SkyBlockMod.petDisplayViewOff = !SkyBlockMod.petDisplayViewOff;
            button.displayString = "§e[Pet] §7Visibilidade de Pet: " + getStatus(SkyBlockMod.petDisplayViewOff);
        } else if (button.id == 3) {
            SkyBlockMod.viewsPetAll = !SkyBlockMod.viewsPetAll;
            button.displayString = "§e[Pet] §7Remover todos os Pets: " + getStatus(SkyBlockMod.viewsPetAll);
        } else if (button.id == 10) {
            mc.displayGuiScreen(null);
            SkyBlockMod.pendingOpenMenu = true;
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

        this.drawCenteredString(this.fontRendererObj, "§6SkyBlockModVX §f– §7Editor de Pet", this.width / 2, 20, (buttonAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
