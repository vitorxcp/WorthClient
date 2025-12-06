package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GuiCustomMultiplayer extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    private long animationStartTime;
    private boolean isOpening, isClosing;
    private final int ANIMATION_DURATION_MS = 500;
    private GuiScreen nextScreen = null;

    private final GuiScreen parentScreen;
    private ServerList savedServerList;
    private GuiCustomServerList serverListSelector;
    private int selectedServer = -1;
    private final OldServerPinger oldServerPinger = new OldServerPinger();
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    private ScheduledExecutorService pingExecutor;

    private GuiButton btnSelectServer, btnEditServer, btnDeleteServer;

    public GuiCustomMultiplayer(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        this.isOpening = true;
        this.isClosing = false;
        this.animationStartTime = System.currentTimeMillis();

        this.savedServerList = new ServerList(this.mc);
        this.savedServerList.loadServerList();

        try {
            this.lanServerList = new LanServerDetector.LanServerList();
            this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
            this.lanServerDetector.start();
        } catch (Exception ignored) {}

        this.serverListSelector = new GuiCustomServerList(this, this.mc, this.width, this.height, 32, this.height - 64, 44);
        this.serverListSelector.setServerList(this.savedServerList);

        createButtons();
        startPingExecutor();
    }

    private void createButtons() {
        int btnWidth = 100;
        int btnSpacing = 6;
        int bottomRowY = this.height - 54;
        int bottomRowY2 = this.height - 30;

        int totalWidthRow1 = (btnWidth * 3) + (btnSpacing * 2);
        int startX = (this.width - totalWidthRow1) / 2;

        this.buttonList.add(this.btnSelectServer = new GuiModernButton(1, startX, bottomRowY, btnWidth, 20, "Entrar", 0L));
        this.buttonList.add(new GuiModernButton(2, startX + btnWidth + btnSpacing, bottomRowY, btnWidth, 20, "Conexão Direta", 100L));
        this.buttonList.add(new GuiModernButton(7, startX + (btnWidth + btnSpacing) * 2, bottomRowY, btnWidth, 20, "Atualizar", 200L));

        this.buttonList.add(new GuiModernButton(8, startX, bottomRowY2, btnWidth, 20, "Adicionar", 300L));
        this.buttonList.add(this.btnEditServer = new GuiModernButton(4, startX + btnWidth + btnSpacing, bottomRowY2, btnWidth, 20, "Editar", 400L));
        this.buttonList.add(this.btnDeleteServer = new GuiModernButton(3, startX + (btnWidth + btnSpacing) * 2, bottomRowY2, btnWidth, 20, "Deletar", 500L));

        this.buttonList.add(new GuiModernButton(0, 10, 10, 60, 20, "Voltar", 600L));

        this.updateButtonStates();
    }

    private void triggerExitAnimation(GuiScreen screenToOpen) {
        if (!this.isClosing) {
            this.isClosing = true;
            this.isOpening = false;
            this.animationStartTime = System.currentTimeMillis();
            this.nextScreen = screenToOpen;
        }
    }

    private void drawCustomBackground() {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        drawRect(0, 0, this.width, this.height, new Color(15, 15, 20, 120).getRGB());
    }

    private void drawSlidingBarsTransition(float progress) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        Color goldDark = new Color(139, 105, 20, 255);
        Color goldLight = new Color(255, 215, 0, 255);

        drawRect(0, 0, this.width, this.height, new Color(0, 0, 0, (int)(255 * progress)).getRGB());

        int numBars = 3;
        float barHeight = (float) this.height / numBars;
        float totalWidthToCover = this.width + 100;

        for (int i = 0; i < numBars; i++) {
            float startY = barHeight * i;
            float endY = barHeight * (i + 1);
            float offset = (totalWidthToCover * progress);

            float x1 = (i % 2 == 0) ? -totalWidthToCover + offset : totalWidthToCover - offset;
            float x2 = x1 + totalWidthToCover;

            worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            addVertexWithColor(worldrenderer, x1, startY, goldDark);
            addVertexWithColor(worldrenderer, x2, startY, goldLight);
            addVertexWithColor(worldrenderer, x2, endY, goldLight);
            addVertexWithColor(worldrenderer, x1, endY, goldDark);
            tessellator.draw();
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void addVertexWithColor(WorldRenderer wr, float x, float y, Color c) {
        wr.pos(x, y, 0).color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f).endVertex();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float progress = 0;
        if (isOpening || isClosing) {
            long elapsedTime = System.currentTimeMillis() - this.animationStartTime;
            progress = Math.min(1.0f, (float)elapsedTime / (float)this.ANIMATION_DURATION_MS);
        }
        float easedProgress = AnimationUtil.easeOutCubic(progress);

        drawCustomBackground();

        if (this.serverListSelector != null) {
            this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        }

        float uiAlpha = 1.0f;
        if (isOpening) uiAlpha = easedProgress;
        if (isClosing) uiAlpha = 1.0f - easedProgress;
        uiAlpha = Math.max(0.0f, uiAlpha);

        this.drawCenteredString(this.fontRendererObj, "Multijogador", this.width / 2, 15, new Color(1f, 1f, 1f, uiAlpha).getRGB());

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) {
                ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, uiAlpha);
            } else {
                button.drawButton(this.mc, mouseX, mouseY);
            }
        }

        if (isOpening || isClosing) {
            float transitionEffectProgress = isOpening ? 1.0f - easedProgress : easedProgress;
            drawSlidingBarsTransition(transitionEffectProgress);

            if (progress >= 1.0f) {
                if (isOpening) isOpening = false;
                if (isClosing) {
                    if (this.nextScreen == null) this.mc.shutdown();
                    else this.mc.displayGuiScreen(this.nextScreen);
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled || isClosing || isOpening) return;

        switch (button.id) {
            case 0:
                triggerExitAnimation(this.parentScreen);
                break;
            case 1:
                connectToSelected();
                break;
            case 2:
                this.mc.displayGuiScreen(new GuiScreenServerList(this, new ServerData("", "", false)));
                break;
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
            case 7:
                this.refreshServerList();
                break;
            case 8:
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, new ServerData("Servidor de Minecraft", "", false)));
                break;
        }
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
                try { oldServerPinger.ping(server); } catch (Exception ignored) {}
            }
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

    public void updateButtonStates() {
        boolean isServerSelected = this.selectedServer >= 0 && this.selectedServer < this.serverListSelector.getSize();
        this.btnSelectServer.enabled = isServerSelected;
        this.btnEditServer.enabled = isServerSelected;
        this.btnDeleteServer.enabled = isServerSelected;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
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

    public OldServerPinger getOldServerPinger() { return this.oldServerPinger; }
    public boolean isSelected(int index) { return index == this.selectedServer; }
}