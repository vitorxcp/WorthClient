package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.config.KeystrokesColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class GuiKeystrokesColorEditor extends GuiScreen {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void initGui() {
        buttonList.clear();

        int centerX = width / 2;
        int startY = height / 4;

        buttonList.add(new GuiButton(0, centerX - 100, startY, 200, 20, "Cor: Fundo Padrão"));
        buttonList.add(new GuiButton(1, centerX - 100, startY + 24, 200, 20, "Cor: Fundo Pressionado"));
        buttonList.add(new GuiButton(2, centerX - 100, startY + 48, 200, 20, "Cor: Borda"));
        buttonList.add(new GuiButton(3, centerX - 100, startY + 72, 200, 20, "Cor: Texto"));
        buttonList.add(new GuiButton(4, centerX - 100, startY + 96, 200, 20, "Cor: Texto CPS"));

        buttonList.add(new GuiButton(5, centerX - 100, startY + 140, 98, 20, "Salvar"));
        buttonList.add(new GuiButton(6, centerX + 2, startY + 140, 98, 20, "Resetar"));
        buttonList.add(new GuiButton(7, centerX - 100, startY + 170, 200, 20, "Voltar"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                escolherCor("Fundo Padrão", KeystrokesColors.backgroundDefault, KeystrokesColors::setBackgroundDefault);
                break;
            case 1:
                escolherCor("Fundo Pressionado", KeystrokesColors.backgroundPressed, KeystrokesColors::setBackgroundPressed);
                break;
            case 2:
                escolherCor("Borda", KeystrokesColors.border, KeystrokesColors::setBorder);
                break;
            case 3:
                escolherCor("Texto", KeystrokesColors.text, KeystrokesColors::setText);
                break;
            case 4:
                escolherCor("Texto CPS", KeystrokesColors.cpsText, KeystrokesColors::setCpsText);
                break;
            case 5:
                KeystrokesColors.saveColors();
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("§aCores salvas com sucesso!"));
                break;
            case 6:
                KeystrokesColors.resetToDefault();
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("§cCores resetadas para o padrão."));
                break;
            case 7:
                mc.displayGuiScreen(null);
                com.vitorxp.WorthClient.WorthClient.GuiOverlay = true;
                break;
        }
    }

    private void escolherCor(String titulo, Color atual, java.util.function.Consumer<Color> setter) {
        Color novaCor = JColorChooser.showDialog(null, "Escolha a cor para " + titulo, atual);
        if (novaCor != null) setter.accept(novaCor);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "WorthClient - Editor de Cores do Keystrokes HUD", width / 2, 15, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);

        drawColorBox(width / 2 - 110, height / 4, KeystrokesColors.backgroundDefault);
        drawColorBox(width / 2 - 110, height / 4 + 24, KeystrokesColors.backgroundPressed);
        drawColorBox(width / 2 - 110, height / 4 + 48, KeystrokesColors.border);
        drawColorBox(width / 2 - 110, height / 4 + 72, KeystrokesColors.text);
        drawColorBox(width / 2 - 110, height / 4 + 96, KeystrokesColors.cpsText);
    }

    private void drawColorBox(int x, int y, Color color) {
        drawRect(x, y, x + 10, y + 10, color.getRGB());
    }
}