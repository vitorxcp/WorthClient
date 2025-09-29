package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PainelAdminGui extends GuiScreen {

    private List<String> playerNames;
    private int currentPage = 0;
    private final int playersPerPage = 10;
    private int totalPages;

    private GuiButton prevButton;
    private GuiButton nextButton;

    public PainelAdminGui() {
        NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
        if (netHandler != null) {
            this.playerNames = netHandler.getPlayerInfoMap().stream()
                    .map(info -> info.getGameProfile().getName())
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .collect(Collectors.toList());
        } else {
            this.playerNames = new ArrayList<>();
        }

        this.totalPages = (int) Math.ceil((double) this.playerNames.size() / playersPerPage);
        if (this.totalPages == 0) {
            this.totalPages = 1;
        }
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        int startY = this.height / 2 - (playersPerPage * 22) / 2;
        int startX = this.width / 2 - 100;
        int startIndex = currentPage * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, playerNames.size());

        for (int i = startIndex; i < endIndex; i++) {
            String playerName = playerNames.get(i);
            int buttonId = i + 100;
            int buttonY = startY + (i - startIndex) * 22;
            this.buttonList.add(new GuiButton(buttonId, startX, buttonY, 200, 20, playerName));
        }

        int bottomY = startY + (playersPerPage * 22) + 5;
        prevButton = new GuiButton(1, this.width / 2 - 100, bottomY, 98, 20, "§c< Página Anterior");
        nextButton = new GuiButton(2, this.width / 2 + 2, bottomY, 98, 20, "§aPróxima Página >");

        this.buttonList.add(prevButton);
        this.buttonList.add(nextButton);

        updatePageButtons();
    }

    private void updatePageButtons() {
        prevButton.enabled = currentPage > 0;
        nextButton.enabled = currentPage < totalPages - 1;
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            if (currentPage > 0) {
                currentPage--;
                initGui();
            }
        }
        else if (button.id == 2) {
            if (currentPage < totalPages - 1) {
                currentPage++;
                initGui();
            }
        }
        else if (button.id >= 100) {
            String targetPlayerName = button.displayString;
            this.mc.displayGuiScreen(new AdminGui(targetPlayerName));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        String title = "Painel de Administração - Jogadores";
        String pageInfo = String.format("Página %d de %d", currentPage + 1, totalPages);

        drawCenteredString(this.fontRendererObj, title, this.width / 2, 20, 0xFFFFFF);
        drawCenteredString(this.fontRendererObj, pageInfo, this.width / 2, 35, 0xAAAAAA);

        if (playerNames.isEmpty()) {
            drawCenteredString(this.fontRendererObj, "§cNenhum jogador válido online encontrado.", this.width / 2, this.height / 2, 0xFF5555);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}