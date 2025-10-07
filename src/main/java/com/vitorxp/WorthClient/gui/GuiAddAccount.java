package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.account.SessionManager;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;

public class GuiAddAccount extends GuiScreen {
    private final GuiScreen parentScreen;
    private GuiTextField usernameField;
    private GuiTextField passwordField;
    private String status = "";

    public GuiAddAccount(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.buttonList.add(new GuiModernButton(0, centerX - 100, centerY - 60, 200, 20, "§bEntrar com Microsoft (Recomendado)", 0L));

        this.usernameField = new GuiTextField(1, this.fontRendererObj, centerX - 100, centerY - 10, 200, 20);
        this.passwordField = new GuiTextField(2, this.fontRendererObj, centerX - 100, centerY + 20, 200, 20);
        this.usernameField.setMaxStringLength(100);
        this.passwordField.setMaxStringLength(100);

        this.buttonList.add(new GuiModernButton(3, centerX - 100, centerY + 50, 200, 20, "Login Pirata / Mojang (Inseguro)", 0L));
        this.buttonList.add(new GuiModernButton(4, centerX - 100, centerY + 75, 200, 20, "Cancelar", 0L));
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.usernameField.textboxKeyTyped(typedChar, keyCode);
        this.passwordField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        this.usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        this.passwordField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(new GuiMicrosoftLogin(this.parentScreen));
        } else if (button.id == 3) {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (user.isEmpty()) {
                this.status = "§cUsuário não pode ser vazio.";
                return;
            }
            if (pass.isEmpty()) {
                SessionManager.loginCracked(user);
                this.mc.displayGuiScreen(this.parentScreen);
            } else {
                button.enabled = false;
                new Thread(() -> {
                    this.status = "§eLogando (Legacy)...";
                    final String loginResult = SessionManager.login(user, pass);
                    this.status = loginResult;
                    if (loginResult.startsWith("§a")) {
                        mc.addScheduledTask(() -> mc.displayGuiScreen(this.parentScreen));
                    } else {
                        mc.addScheduledTask(() -> button.enabled = true);
                    }
                }).start();
            }
        } else if (button.id == 4) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Adicionar Conta", this.width / 2, 40, 0xFFFFFF);

        // Desenha as caixas de texto para os métodos antigos
        this.drawString(this.fontRendererObj, "Nick (Pirata) ou Email (Mojang)", this.width / 2 - 100, this.height / 2 - 25, 0xA0A0A0);
        this.usernameField.drawTextBox();
        this.drawString(this.fontRendererObj, "Senha (Apenas para contas Mojang antigas)", this.width / 2 - 100, this.height / 2 + 10, 0xA0A0A0);
        this.passwordField.drawTextBox();

        this.drawCenteredString(this.fontRendererObj, this.status, this.width / 2, this.height / 2 + 100, 0xFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}