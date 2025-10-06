package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import org.lwjgl.input.Keyboard;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiCustomMultiplayer extends GuiScreen {

    private final GuiScreen parentScreen;
    private ServerList savedServerList;
    private GuiCustomServerList serverListSelector;
    private int selectedServer = -1;
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;

    private GuiButton btnSelectServer, btnEditServer, btnDeleteServer;
    private ScheduledExecutorService pingExecutor;

    public GuiCustomMultiplayer(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        this.savedServerList = new ServerList(this.mc);
        this.savedServerList.loadServerList();

        try {
            this.lanServerList = new LanServerDetector.LanServerList();
            this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
            this.lanServerDetector.start();
        } catch (Exception e) {
            System.err.println("Não foi possível iniciar a detecção de servidores LAN.");
        }

        this.serverListSelector = new GuiCustomServerList(this, this.mc, this.width, this.height, 32, this.height - 64, 44);
        this.serverListSelector.setServerList(this.savedServerList);

        int btnWidth = 100;
        int btnSpacing = 4;
        int totalWidth = (btnWidth * 3) + (btnSpacing * 2);
        int startX = this.width / 2 - totalWidth / 2;
        this.buttonList.add(this.btnSelectServer = new GuiModernButton(1, startX, this.height - 52, btnWidth, 20, "Entrar", 0L));
        this.buttonList.add(new GuiModernButton(2, startX + btnWidth + btnSpacing, this.height - 52, btnWidth, 20, "Conexão Direta", 0L));
        this.buttonList.add(new GuiModernButton(7, startX + (btnWidth + btnSpacing) * 2, this.height - 52, btnWidth, 20, "Atualizar", 0L));
        this.buttonList.add(new GuiModernButton(8, startX, this.height - 28, btnWidth, 20, "Adicionar", 0L));
        this.buttonList.add(this.btnEditServer = new GuiModernButton(4, startX + btnWidth + btnSpacing, this.height - 28, btnWidth, 20, "Editar", 0L));
        this.buttonList.add(this.btnDeleteServer = new GuiModernButton(3, startX + (btnWidth + btnSpacing) * 2, this.height - 28, btnWidth, 20, "Deletar", 0L));
        this.buttonList.add(new GuiModernButton(0, 10, 8, 70, 20, "Voltar", 0L));
        this.updateButtonStates();

        startPingExecutor();
    }

    private void startPingExecutor() {
        if (this.pingExecutor != null && !this.pingExecutor.isShutdown()) {
            this.pingExecutor.shutdownNow();
        }
        this.pingExecutor = Executors.newSingleThreadScheduledExecutor();
        this.pingExecutor.scheduleAtFixedRate(this::pingAllServers, 0, 3, TimeUnit.SECONDS);
    }

    private void pingAllServers() {
        for(int i = 0; i < this.savedServerList.countServers(); i++) {
            ServerData server = this.savedServerList.getServerData(i);
            if (server != null && server.pingToServer != -2L) {
                try {
                    oldServerPinger.ping(server);
                } catch (Exception e) {
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    public void updateButtonStates() {
        boolean isServerSelected = this.selectedServer >= 0 && this.selectedServer < this.serverListSelector.getSize();
        this.btnSelectServer.enabled = isServerSelected;
        this.btnEditServer.enabled = isServerSelected;
        this.btnDeleteServer.enabled = isServerSelected;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.lanServerList.getWasUpdated()) {
            List<LanServerDetector.LanServer> lanServers = this.lanServerList.getLanServers();
            this.lanServerList.setWasNotUpdated();
            this.serverListSelector.setLanServers(lanServers);
        }
        this.oldServerPinger.pingPendingNetworks();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (this.lanServerDetector != null) this.lanServerDetector.interrupt();
        this.oldServerPinger.clearPendingNetworks();
        if (this.serverListSelector != null) this.serverListSelector.cleanup();

        if (this.pingExecutor != null) this.pingExecutor.shutdownNow();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        switch (button.id) {
            case 0: this.mc.displayGuiScreen(this.parentScreen); break;
            case 1: connectToSelected(); break;
            case 2: this.mc.displayGuiScreen(new GuiScreenServerList(this, new ServerData("", "", false))); break;
            case 3:
                if (this.selectedServer >= 0 && this.selectedServer < this.serverListSelector.getSize()) {
                    String serverName = this.serverListSelector.getServerData(this.selectedServer).serverName;
                    this.mc.displayGuiScreen(new GuiYesNo(this, "Tem certeza que quer remover este servidor?", "'" + serverName + "' será perdido para sempre!", "Deletar", "Cancelar", this.selectedServer));
                }
                break;
            case 4:
                if (this.selectedServer >= 0 && this.selectedServer < this.serverListSelector.getSize()) {
                    this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.serverListSelector.getServerData(this.selectedServer)));
                }
                break;
            case 7: this.refreshServerList(); break;
            case 8: this.mc.displayGuiScreen(new GuiScreenAddServer(this, new ServerData("Servidor de Minecraft", "", false))); break;
        }
    }

    private void refreshServerList() {
        this.mc.displayGuiScreen(new GuiCustomMultiplayer(this.parentScreen));
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (result && id < this.savedServerList.countServers()) {
            this.savedServerList.removeServerData(id);
            this.savedServerList.saveServerList();
            this.selectServer(-1);
            this.serverListSelector.setServerList(this.savedServerList);
        }
        this.mc.displayGuiScreen(this);
    }

    public void connectToSelected() {
        if (this.selectedServer >= 0 && this.selectedServer < this.serverListSelector.getSize()) {
            this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.serverListSelector.getServerData(this.selectedServer)));
        }
    }

    public void selectServer(int index) {
        this.selectedServer = index;
        this.updateButtonStates();
    }

    public OldServerPinger getOldServerPinger() { return this.oldServerPinger; }
    public boolean isSelected(int index) { return index == this.selectedServer; }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        if (this.serverListSelector != null) this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Multijogador", this.width / 2, 15, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}