package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.manager.ConfigManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class GuiEditorArmor extends GuiScreen {

    private GuiButton btnMainHand, btnHelmet, btnChestplate, btnLeggings, btnBoots, btnReturn;
    private float alpha = 0f;
    private boolean fadeIn = true;
    int buttonAlpha = 0;
    @Override
    public void initGui() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.clear();

        int y = centerY - 100;

        btnMainHand = new GuiButton(1, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Item na Mão: " + getStatus(SkyBlockMod.mainHandHUDOverlay));
        this.buttonList.add(btnMainHand); y += 25;

        btnHelmet = new GuiButton(2, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Capacete: " + getStatus(SkyBlockMod.helmetHUDOverlay));
        this.buttonList.add(btnHelmet); y += 25;

        btnChestplate = new GuiButton(3, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Peitoral: " + getStatus(SkyBlockMod.chestplateHUDOverlay));
        this.buttonList.add(btnChestplate); y += 25;

        btnLeggings = new GuiButton(4, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Calças: " + getStatus(SkyBlockMod.leggingsHUDOverlay));
        this.buttonList.add(btnLeggings); y += 25;

        btnBoots = new GuiButton(5, centerX - 125, y, 250, 20,
                "§b[Overlay] §7Mostrar Botas: " + getStatus(SkyBlockMod.bootsHUDOverlay));
        this.buttonList.add(btnBoots); y += 25;

        btnReturn = new GuiButton(10, centerX - 125, y, 250, 20,
                "Voltar");
        this.buttonList.add(btnReturn); y += 25;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            SkyBlockMod.mainHandHUDOverlay = !SkyBlockMod.mainHandHUDOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Item na Mão: " + getStatus(SkyBlockMod.mainHandHUDOverlay);
        } else if (button.id == 2) {
            SkyBlockMod.helmetHUDOverlay = !SkyBlockMod.helmetHUDOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Capacete: " + getStatus(SkyBlockMod.helmetHUDOverlay);
        } else if (button.id == 3) {
            SkyBlockMod.chestplateHUDOverlay = !SkyBlockMod.chestplateHUDOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Peitoral: " + getStatus(SkyBlockMod.chestplateHUDOverlay);
        } else if (button.id == 4) {
            SkyBlockMod.leggingsHUDOverlay = !SkyBlockMod.leggingsHUDOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Calças: " + getStatus(SkyBlockMod.leggingsHUDOverlay);
        } else if (button.id == 5) {
            SkyBlockMod.bootsHUDOverlay = !SkyBlockMod.bootsHUDOverlay;
            button.displayString = "§b[Overlay] §7Mostrar Botas: " + getStatus(SkyBlockMod.bootsHUDOverlay);
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

        this.drawCenteredString(this.fontRendererObj, "§6WorthMod §f– §7Editor de Armaduras", this.width / 2, 20, (buttonAlpha << 24) | 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}
