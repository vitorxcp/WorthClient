package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.manager.AutoLoginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class GuiAutoLoginConfig extends GuiScreen {

    private final GuiScreen parent;
    private final AutoLoginManager.ServerConfig currentConfig;

    private final Color themeColor = new Color(158, 96, 32);
    private final int colBackgroundTop = 0xF0141414;
    private final int colBackgroundBottom = 0xF0230F05;

    private int guiWidth = 600;
    private int guiHeight = 420;
    private int guiLeft;
    private int guiTop;
    private float currentScale = 0.0f;
    private boolean closing = false;
    private float scrollOffset = 0;
    private float maxScroll = 0;

    private GuiTextField ipField, commandField, newUserField, newPassField;

    public GuiAutoLoginConfig(GuiScreen parent, AutoLoginManager.ServerConfig config) {
        this.parent = parent;
        this.currentConfig = config;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;
        this.currentScale = 0.0f;
        this.closing = false;
        Keyboard.enableRepeatEvents(true);

        ipField = new GuiTextField(0, fontRendererObj, guiLeft + 40, guiTop + 60, 250, 20);
        ipField.setEnableBackgroundDrawing(false);
        commandField = new GuiTextField(1, fontRendererObj, guiLeft + 310, guiTop + 60, 250, 20);
        commandField.setEnableBackgroundDrawing(false);

        newUserField = new GuiTextField(2, fontRendererObj, guiLeft + 40, guiTop + 140, 200, 20);
        newUserField.setEnableBackgroundDrawing(false);
        newUserField.setMaxStringLength(32);
        newUserField.setText("Nick");

        newPassField = new GuiTextField(3, fontRendererObj, guiLeft + 260, guiTop + 140, 200, 20);
        newPassField.setEnableBackgroundDrawing(false);
        newPassField.setMaxStringLength(50);
        newPassField.setText("Senha");

        if (currentConfig != null) {
            ipField.setText(currentConfig.serverIP);
            commandField.setText(currentConfig.loginCommand);
        } else {
            ipField.setText("ip.servidor.com");
            commandField.setText("/login {password}");
        }
    }

    @Override
    public void onGuiClosed() { Keyboard.enableRepeatEvents(false); }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) scrollOffset += 25; else scrollOffset -= 25;
            if (scrollOffset > 0) scrollOffset = 0;
            if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x50000000);

        if (closing) {
            currentScale = lerp(currentScale, 0f, 0.5f);
            if (currentScale < 0.1f) {
                mc.displayGuiScreen(parent);
                return;
            }
        } else {
            currentScale = lerp(currentScale, 1f, 0.4f);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2f, height / 2f, 0);
        GlStateManager.scale(currentScale, currentScale, 1f);
        GlStateManager.translate(-width / 2f, -height / 2f, 0);

        GuiAutoLoginServers.drawRoundedRect(guiLeft, guiTop, guiWidth, guiHeight, 15, colBackgroundTop);
        GuiAutoLoginServers.drawRoundedOutline(guiLeft, guiTop, guiWidth, guiHeight, 15, 2.0f, themeColor.getRGB());

        drawCenteredString(fontRendererObj, currentConfig == null ? "Novo Servidor" : "Editando: " + currentConfig.serverIP, width / 2, guiTop + 15, themeColor.getRGB());

        boolean hovBack = isHover(mouseX, mouseY, guiLeft + 20, guiTop + 15, 50, 20);
        GuiAutoLoginServers.drawRoundedRect(guiLeft + 20, guiTop + 15, 50, 20, 5, hovBack ? 0xFF555555 : 0xFF333333);
        drawCenteredString(fontRendererObj, "< Voltar", guiLeft + 45, guiTop + 21, 0xFFFFFFFF);

        boolean hovSave = isHover(mouseX, mouseY, guiLeft + guiWidth - 80, guiTop + 15, 60, 20);
        GuiAutoLoginServers.drawRoundedRect(guiLeft + guiWidth - 80, guiTop + 15, 60, 20, 5, hovSave ? 0xFF2ECC71 : 0xFF27AE60);
        drawCenteredString(fontRendererObj, "Salvar", guiLeft + guiWidth - 50, guiTop + 21, 0xFFFFFFFF);

        if (currentConfig != null) {
            boolean hovDel = isHover(mouseX, mouseY, guiLeft + guiWidth - 150, guiTop + 15, 60, 20);
            GuiAutoLoginServers.drawRoundedRect(guiLeft + guiWidth - 150, guiTop + 15, 60, 20, 5, hovDel ? 0xFFE74C3C : 0xFFC0392B);
            drawCenteredString(fontRendererObj, "Excluir", guiLeft + guiWidth - 120, guiTop + 21, 0xFFFFFFFF);
        }

        fontRendererObj.drawString("IP do Servidor:", guiLeft + 40, guiTop + 48, 0xFFAAAAAA);
        GuiAutoLoginServers.drawRoundedRect(guiLeft + 35, guiTop + 58, 260, 24, 6, 0xFF151515);
        ipField.drawTextBox();

        fontRendererObj.drawString("Comando:", guiLeft + 310, guiTop + 48, 0xFFAAAAAA);
        GuiAutoLoginServers.drawRoundedRect(guiLeft + 305, guiTop + 58, 260, 24, 6, 0xFF151515);
        commandField.drawTextBox();

        drawRect(guiLeft + 20, guiTop + 95, guiLeft + guiWidth - 20, guiTop + 96, 0x40FFFFFF);

        fontRendererObj.drawString("Adicionar / Atualizar Conta:", guiLeft + 40, guiTop + 105, 0xFFFFFFFF);

        GuiAutoLoginServers.drawRoundedRect(guiLeft + 35, guiTop + 135, 210, 24, 6, 0xFF151515);
        newUserField.drawTextBox();
        if(newUserField.getText().isEmpty()) fontRendererObj.drawString("Nick", guiLeft + 40, guiTop + 140, 0xFF555555);

        GuiAutoLoginServers.drawRoundedRect(guiLeft + 255, guiTop + 135, 210, 24, 6, 0xFF151515);
        newPassField.drawTextBox();
        if(newPassField.getText().isEmpty()) fontRendererObj.drawString("Senha", guiLeft + 260, guiTop + 140, 0xFF555555);

        boolean hovAdd = isHover(mouseX, mouseY, guiLeft + 480, guiTop + 135, 80, 24);
        GuiAutoLoginServers.drawRoundedRect(guiLeft + 480, guiTop + 135, 80, 24, 6, hovAdd ? 0xFFFFAA00 : 0xFF9E6020);
        drawCenteredString(fontRendererObj, "Adicionar", guiLeft + 520, guiTop + 143, 0xFFFFFFFF);

        int listY = guiTop + 180;
        int listH = guiHeight - 200;
        int itemH = 30;

        if (currentConfig != null) {
            int totalH = currentConfig.accounts.size() * (itemH + 5);
            maxScroll = Math.max(0, totalH - listH);

            int scale = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((guiLeft + 30) * scale, (mc.displayHeight - (listY + listH) * scale), (guiWidth - 60) * scale, listH * scale);

            int currentY = (int) (listY + scrollOffset);

            if (currentConfig.accounts.isEmpty()) {
                drawCenteredString(fontRendererObj, "Nenhuma conta salva neste servidor.", width / 2, listY + 20, 0xFFAAAAAA);
            } else {
                for (int i = 0; i < currentConfig.accounts.size(); i++) {
                    AutoLoginManager.AccountEntry acc = currentConfig.accounts.get(i);

                    if (currentY + itemH > listY && currentY < listY + listH) {
                        int rowX = guiLeft + 30;
                        int rowW = guiWidth - 60;
                        boolean hovRow = isHover(mouseX, mouseY, rowX, currentY, rowW, itemH);

                        GuiAutoLoginServers.drawRoundedRect(rowX, currentY, rowW, itemH, 6, hovRow ? 0xFF353535 : 0xFF252525);

                        fontRendererObj.drawString(acc.username, rowX + 15, currentY + 11, 0xFFFFFFFF);
                        fontRendererObj.drawString("(****)", rowX + 150, currentY + 11, 0xFFAAAAAA);

                        if (hovRow) fontRendererObj.drawString("CLIQUE P/ EDITAR", rowX + 250, currentY + 11, themeColor.getRGB());

                        boolean hovDelRow = isHover(mouseX, mouseY, rowX + rowW - 30, currentY + 5, 20, 20);
                        GuiAutoLoginServers.drawRoundedRect(rowX + rowW - 30, currentY + 5, 20, 20, 4, hovDelRow ? 0xFFE74C3C : 0xFFC0392B);
                        drawCenteredString(fontRendererObj, "X", rowX + rowW - 20, currentY + 11, 0xFFFFFFFF);
                    }
                    currentY += (itemH + 5);
                }
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        } else {
            drawCenteredString(fontRendererObj, "Salve o servidor para adicionar contas.", width / 2, listY + 50, 0xFFAAAAAA);
        }

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ipField.mouseClicked(mouseX, mouseY, mouseButton);
        commandField.mouseClicked(mouseX, mouseY, mouseButton);
        newUserField.mouseClicked(mouseX, mouseY, mouseButton);
        newPassField.mouseClicked(mouseX, mouseY, mouseButton);

        if (isHover(mouseX, mouseY, guiLeft + 20, guiTop + 15, 50, 20)) {
            closing = true;
            return;
        }

        if (currentConfig != null && isHover(mouseX, mouseY, guiLeft + guiWidth - 150, guiTop + 15, 60, 20)) {
            AutoLoginManager.servers.remove(currentConfig);
            AutoLoginManager.save();
            NotificationRenderer.send(NotificationRenderer.Type.WARNING, "Servidor removido.");
            closing = true;
            return;
        }

        if (isHover(mouseX, mouseY, guiLeft + guiWidth - 80, guiTop + 15, 60, 20)) {
            String ip = ipField.getText();
            String cmd = commandField.getText();

            if (ip.isEmpty() || cmd.isEmpty()) {
                NotificationRenderer.send(NotificationRenderer.Type.ERROR, "IP ou Comando inválidos!");
                return;
            }

            if (currentConfig == null) {
                if (AutoLoginManager.getServerConfig(ip) != null) {
                    NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Servidor já existe!");
                    return;
                }
                AutoLoginManager.ServerConfig newCfg = new AutoLoginManager.ServerConfig(ip, cmd);
                if(!newUserField.getText().equals("Nick") && !newPassField.getText().equals("Senha") && !newUserField.getText().isEmpty()) {
                    newCfg.addAccount(newUserField.getText(), newPassField.getText());
                }
                AutoLoginManager.servers.add(newCfg);
            } else {
                currentConfig.serverIP = ip;
                currentConfig.loginCommand = cmd;
                if(!newUserField.getText().equals("Nick") && !newPassField.getText().equals("Senha") && !newUserField.getText().isEmpty()) {
                    currentConfig.addAccount(newUserField.getText(), newPassField.getText());
                }
            }
            AutoLoginManager.save();
            NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Servidor salvo!");
            closing = true;
            return;
        }

        if (isHover(mouseX, mouseY, guiLeft + 480, guiTop + 135, 80, 24)) {
            if (currentConfig != null) {
                String u = newUserField.getText();
                String p = newPassField.getText();
                if(u.isEmpty() || p.isEmpty() || u.equals("Nick") || p.equals("Senha")) {
                    NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Preencha Nick e Senha!");
                    return;
                }
                currentConfig.addAccount(u, p);
                AutoLoginManager.save();
                NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Conta salva!");
                newUserField.setText(""); newPassField.setText("");
            } else {
                NotificationRenderer.send(NotificationRenderer.Type.WARNING, "Salve o servidor primeiro!");
            }
            return;
        }

        if (currentConfig != null) {
            int listY = guiTop + 180;
            int listH = guiHeight - 200;
            int itemH = 30;
            int currentY = (int) (listY + scrollOffset);

            if (mouseY >= listY && mouseY <= listY + listH) {
                for (int i = 0; i < currentConfig.accounts.size(); i++) {
                    int rowX = guiLeft + 30;
                    int rowW = guiWidth - 60;

                    if (isHover(mouseX, mouseY, rowX + rowW - 30, currentY + 5, 20, 20)) {
                        currentConfig.accounts.remove(i);
                        AutoLoginManager.save();
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                        return;
                    }

                    if (isHover(mouseX, mouseY, rowX, currentY, rowW - 40, itemH)) {
                        AutoLoginManager.AccountEntry acc = currentConfig.accounts.get(i);
                        newUserField.setText(acc.username);
                        newPassField.setText(acc.password);
                        mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    }
                    currentY += (itemH + 5);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode == 1) closing = true;
        ipField.textboxKeyTyped(typedChar, keyCode);
        commandField.textboxKeyTyped(typedChar, keyCode);
        newUserField.textboxKeyTyped(typedChar, keyCode);
        newPassField.textboxKeyTyped(typedChar, keyCode);
    }

    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private float lerp(float a, float b, float f) { return a + f * (b - a); }
}