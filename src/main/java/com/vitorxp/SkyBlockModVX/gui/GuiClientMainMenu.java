// Pacote: com.vitorxp.SkyBlockModVX.gui
package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.gui.button.GuiIconButton;
import com.vitorxp.SkyBlockModVX.gui.button.GuiModernButton;
import com.vitorxp.SkyBlockModVX.gui.utils.GuiUtils;
import com.vitorxp.SkyBlockModVX.gui.utils.RenderUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.net.UnknownHostException;

public class GuiClientMainMenu extends GuiScreen {

    // --- RECURSOS ---
    private static final ResourceLocation BACKGROUND = new ResourceLocation("skyblockmodvx", "textures/gui/Background_3.png");
    private static final ResourceLocation MAIN_LOGO = new ResourceLocation("skyblockmodvx", "textures/gui/logo_main.png");
    private static final ResourceLocation DISCORD_ICON = new ResourceLocation("skyblockmodvx", "textures/gui/icons/discord.png");

    // --- PING DO SERVIDOR ---
    private final OldServerPinger serverPinger = new OldServerPinger();
    private ServerData serverData;
    private boolean hasPingBeenSent = false;
    private long motdReceivedTime = -1;
    private float motdAlpha = 0.0f;

    @Override
    public void initGui() {
        this.buttonList.clear();

        // Posição inicial dos botões principais, calculada para dar espaço ao MOTD
        int mainButtonsY = this.height / 2;
        int buttonSpacing = 28;

        this.buttonList.add(new GuiModernButton(0, this.width / 2 - 100, mainButtonsY, "Um Jogador", 200L));
        this.buttonList.add(new GuiModernButton(1, this.width / 2 - 100, mainButtonsY + buttonSpacing, "Multiplayer", 300L));
        this.buttonList.add(new GuiModernButton(2, this.width / 2 - 100, mainButtonsY + buttonSpacing * 2, "Opções", 400L));

        // Botões inferiores e de ícone
        this.buttonList.add(new GuiModernButton(3, this.width / 2 - 100, this.height - 40, 100, 20, "Sair do Jogo", 500L));
        this.buttonList.add(new GuiIconButton(5, 10, this.height - 34, 24, 24, DISCORD_ICON));

        // Configura o servidor-alvo
        this.serverData = new ServerData("Seu Servidor", "redeworth.com", false); // <-- TROQUE O IP AQUI
    }

    @Override
    public void onGuiClosed() {
        this.serverPinger.clearPendingNetworks();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Correção definitiva: só envia o ping uma vez, quando a tela já está ativa
        if (!this.hasPingBeenSent) {
            this.pingServer();
            this.hasPingBeenSent = true;
        }
        this.serverPinger.pingPendingNetworks();
    }

    // Método para encapsular e reutilizar a lógica de ping
    private void pingServer() {
        new Thread(() -> {
            try {
                // Reseta o estado para a animação de fade-in funcionar de novo
                this.motdReceivedTime = -1;
                this.motdAlpha = 0.0f;
                serverPinger.ping(this.serverData);
            } catch (Exception e) {
                this.serverData.serverMOTD = "§cFalha ao buscar informações do servidor.";
                this.serverData.populationInfo = "§cOffline";
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Lógica de clique para o painel do MOTD
        if (this.serverData != null && this.serverData.pingToServer > -1L && this.motdAlpha > 0.9f) {
            int logoHeight = 97 / 2;
            int logoY = this.height / 4 - logoHeight / 2 - 20;

            int panelWidth = 300;
            int panelHeight = 45;
            int panelX = this.width / 2 - panelWidth / 2;
            int panelY = logoY + logoHeight + 25;

            if (mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight) {
                this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.serverData));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fundo e Logo
        drawDefaultBackground();
        drawLogo(partialTicks);

        // Painel de Status do Servidor (MOTD Clicável)
        drawServerStatusPanel(mouseX, mouseY);

        // Botões e Copyright
        for (net.minecraft.client.gui.GuiButton button : this.buttonList) {
            button.drawButton(this.mc, mouseX, mouseY);
        }
        this.drawCenteredString(this.fontRendererObj, "Criado por: vitorxp", this.width / 2, this.height - 15, 0x888888);
    }

    private void drawServerStatusPanel(int mouseX, int mouseY) {
        if (this.serverData != null && this.serverData.pingToServer > -1L) {
            if (this.motdReceivedTime == -1) this.motdReceivedTime = System.currentTimeMillis();
            long timeSinceReceived = System.currentTimeMillis() - this.motdReceivedTime;
            this.motdAlpha = Math.min(1.0f, (float) timeSinceReceived / 1000.0f);

            if (this.motdAlpha > 0) {
                int logoHeight = 97 / 2;
                int logoY = this.height / 4 - logoHeight / 2 - 20;

                int panelWidth = 300;
                int panelHeight = 45;
                int panelX = this.width / 2 - panelWidth / 2;
                int panelY = logoY + logoHeight + 25; // Posição calculada com espaçamento

                // Efeito de hover
                boolean isMouseOver = mouseX >= panelX && mouseX <= panelX + panelWidth && mouseY >= panelY && mouseY <= panelY + panelHeight;
                Color panelColor = isMouseOver ? new Color(0.2f, 0.2f, 0.25f, 0.7f * this.motdAlpha) : new Color(0.1f, 0.1f, 0.15f, 0.6f * this.motdAlpha);

                RenderUtil.drawRoundedRect(panelX, panelY, panelWidth, panelHeight, 5.0f, panelColor.getRGB());

                int textColor = new Color(1.0f, 1.0f, 1.0f, this.motdAlpha).getRGB();

                // Desenha o MOTD centralizado verticalmente
                this.fontRendererObj.drawSplitString(this.serverData.serverMOTD, panelX + 10, panelY + (panelHeight / 2) - 8, panelWidth - 20, textColor);

                // Desenha o número de jogadores
                String players = this.serverData.populationInfo;
                this.fontRendererObj.drawString(players, panelX + panelWidth - this.fontRendererObj.getStringWidth(players) - 10, panelY + (panelHeight / 2) - 13, textColor);

                // Desenha o ping
                String ping = this.serverData.pingToServer + "ms";
                this.fontRendererObj.drawString(ping, panelX + panelWidth - this.fontRendererObj.getStringWidth(ping) - 10, panelY + (panelHeight / 2) + 3, textColor);
            }
        }
    }

    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, new Color(10, 10, 15, 100).getRGB());
    }

    private void drawLogo(float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float yOffset = (float) Math.sin(System.currentTimeMillis() / 800.0F) * 4.0F;
        int logoWidth = 470 / 2;
        int logoHeight = 97 / 2;
        int logoX = this.width / 2 - logoWidth / 2;
        int logoY = this.height / 4 - logoHeight / 2 - 20;
        this.mc.getTextureManager().bindTexture(MAIN_LOGO);
        drawModalRectWithCustomSizedTexture(logoX, (int) (logoY + yOffset), 0, 0, logoWidth, logoHeight, logoWidth, logoHeight);
        GlStateManager.popMatrix();
    }

    @Override
    protected void actionPerformed(net.minecraft.client.gui.GuiButton button) throws IOException {
        switch (button.id) {
            case 0: this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiSelectWorld(this)); break;
            case 1:
                // Atualiza o MOTD e depois abre a tela de multiplayer
                this.pingServer();
                this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiMultiplayer(this));
                break;
            case 2: this.mc.displayGuiScreen(new net.minecraft.client.gui.GuiOptions(this, this.mc.gameSettings)); break;
            case 3: this.mc.shutdown(); break;
            case 5: GuiUtils.openLink("https://discord.gg/VWHvq9zpeV"); break;
        }
    }
}