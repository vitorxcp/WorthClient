package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.function.Consumer;

public class GuiCustomPrompt extends GuiScreen {

    private final GuiScreen parentScreen;
    private final String title;
    private final Consumer<String> callback;
    private GuiTextField inputField;

    public GuiCustomPrompt(GuiScreen parent, String title, Callback callback) {
        this.parentScreen = parent;
        this.title = title;
        this.callback = (Consumer<String>) callback;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.inputField = new GuiTextField(0, this.fontRendererObj, centerX - 100, centerY - 10, 200, 20);
        this.inputField.setFocused(true);
        this.inputField.setMaxStringLength(32);

        this.buttonList.add(new GuiModernButton(0, centerX - 100, centerY + 20, 98, 20, "Confirmar", 0L));
        this.buttonList.add(new GuiModernButton(1, centerX + 2, centerY + 20, 98, 20, "Cancelar", 0L));
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.callback.accept(this.inputField.getText());
        } else if (button.id == 1) {
            this.callback.accept(null);
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.inputField.textboxKeyTyped(typedChar, keyCode)) {
        } else if (keyCode == Keyboard.KEY_RETURN) {
            this.actionPerformed(this.buttonList.get(0));
        } else if (keyCode == Keyboard.KEY_ESCAPE) {
            this.actionPerformed(this.buttonList.get(1));
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.inputField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        this.inputField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public interface Callback {
        void accept(String value);
    }
}