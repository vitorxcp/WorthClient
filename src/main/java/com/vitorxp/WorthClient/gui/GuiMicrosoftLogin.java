package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.account.Account;
import java.awt.Desktop;
import java.net.URI;
import com.vitorxp.WorthClient.account.AccountManager;
import com.vitorxp.WorthClient.account.MicrosoftAuth;
import com.vitorxp.WorthClient.account.SessionManager;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

public class GuiMicrosoftLogin extends GuiScreen {

    private final GuiScreen parentScreen;
    private String status = "";
    private String userCode = null;
    private GuiButton copyButton;
    private GuiButton openLinkButton;

    public GuiMicrosoftLogin(GuiScreen parent) {
        this.parentScreen = parent;
    }

    private void openLink(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.err.println("Não foi possível abrir o link: " + url);
            e.printStackTrace();
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }


    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiModernButton(0, this.width / 2 - 100, this.height - 40, 200, 20, "Cancelar", 0L));
        this.buttonList.add(this.copyButton = new GuiModernButton(1, this.width / 2 - 102, this.height / 2 + 10, 100, 20, "Copiar Código", 0L));
        this.buttonList.add(this.openLinkButton = new GuiModernButton(2, this.width / 2 + 2, this.height / 2 + 10, 100, 20, "Abrir Link", 0L));

        this.copyButton.visible = false;
        this.openLinkButton.visible = false;

        MicrosoftAuth.login(
                s -> this.status = s,
                c -> this.userCode = c
        ).thenAccept(account -> {
            AccountManager.addAccount(account);
            SessionManager.switchAccount(account);
            mc.addScheduledTask(() -> mc.displayGuiScreen(this.parentScreen));
        }).exceptionally(error -> {
            this.status = "§cErro: " + error.getMessage().split("\n")[0];
            return null;
        });
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(parentScreen);
        } else if (button.id == 1) {
            if (this.userCode != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(this.userCode), null);
                button.displayString = "§aCopiado!";
            }
        } else if (button.id == 2) {
            this.openLink("https://microsoft.com/link");
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Login com Microsoft", this.width / 2, 40, 0xFFFFFF);

        this.drawCenteredString(this.fontRendererObj, this.status, this.width / 2, this.height / 2 - 40, 0xAAAAAA);
        if (this.userCode != null) {
            if (copyButton.visible) {
                this.drawCenteredString(this.fontRendererObj, "§l" + this.userCode, this.width / 2, this.height / 2 - 20, 0x55FF55);
            } else {
                this.drawCenteredString(this.fontRendererObj, this.status, this.width / 2, this.height / 2 - 20, 0xAAAAAA);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}