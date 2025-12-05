package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import com.vitorxp.WorthClient.utils.SSLTrustBypasser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.network.OldServerPinger;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class GuiMultiplayerCustom extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");

    private final GuiScreen parentScreen;
    private ServerList savedServerList;
    private CustomServerList serverListSelector;
    private LanServerDetector.LanServerList lanServerList;
    private LanServerDetector.ThreadLanServerFind lanServerDetector;
    private int selectedServer = -1;

    private GuiButton btnSelectServer, btnEditServer, btnDeleteServer;

    private long animationStartTime;
    private boolean isOpening;
    private boolean isClosing;
    private final int ANIMATION_DURATION_MS = 800;
    private GuiScreen nextScreen = null;
    private ExecutorService pingTaskExecutor;
    private final Set<String> favoriteServers = new HashSet<>();
    private final File favoritesFile;
    private boolean isAddingServer = false;
    private boolean isEditingServer = false;
    private boolean isDirectConnecting = false;
    private boolean isDeletingServer = false;
    private ServerData currentServerData;

    public GuiMultiplayerCustom(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;

        SSLTrustBypasser.install();

        this.favoritesFile = new File(Minecraft.getMinecraft().mcDataDir, "worth_favorites.txt");
        loadFavorites();

        this.savedServerList = new ServerList(Minecraft.getMinecraft());
        this.savedServerList.loadServerList();

        checkAndFixRedeWorth();
    }

    private void checkAndFixRedeWorth() {
        boolean found = false;
        for (int i = 0; i < savedServerList.countServers(); i++) {
            ServerData data = savedServerList.getServerData(i);
            if (data.serverIP.equalsIgnoreCase("redeworth.com")) {
                data.serverName = "Rede Worth";
                found = true;
                break;
            }
        }

        if (!found) {
            ServerData rw = new ServerData("Rede Worth", "redeworth.com", false);
            savedServerList.addServerData(rw);
            savedServerList.saveServerList();
        }

        if (!favoriteServers.contains("redeworth.com")) {
            favoriteServers.add("redeworth.com");
            saveFavorites();
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();

        if (!this.isAddingServer && !this.isEditingServer && !this.isDirectConnecting && !this.isDeletingServer) {
            this.isOpening = true;
            this.animationStartTime = System.currentTimeMillis();
        }
        this.isClosing = false;

        this.isAddingServer = false;
        this.isEditingServer = false;
        this.isDirectConnecting = false;
        this.isDeletingServer = false;

        organizeServerList();

        try {
            if (this.lanServerList == null) {
                this.lanServerList = new LanServerDetector.LanServerList();
                this.lanServerDetector = new LanServerDetector.ThreadLanServerFind(this.lanServerList);
                this.lanServerDetector.start();
            }
        } catch (Exception ignored) {}

        this.serverListSelector = new CustomServerList(this.mc, this.width, this.height, 32, this.height - 64, 36);

        createButtons();

        startPingSystem();
    }

    private void loadFavorites() {
        if (!favoritesFile.exists()) return;
        favoriteServers.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(favoritesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    favoriteServers.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFavorites() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(favoritesFile))) {
            for (String ip : favoriteServers) {
                writer.println(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void organizeServerList() {
        try {
            java.lang.reflect.Field serversField = ServerList.class.getDeclaredField("servers");
            serversField.setAccessible(true);
            List<ServerData> originalList = (List<ServerData>) serversField.get(savedServerList);

            Collections.sort(originalList, (s1, s2) -> {
                boolean r1 = s1.serverIP.equalsIgnoreCase("redeworth.com");
                boolean r2 = s2.serverIP.equalsIgnoreCase("redeworth.com");
                if (r1 && !r2) return -1;
                if (!r1 && r2) return 1;

                boolean f1 = favoriteServers.contains(s1.serverIP);
                boolean f2 = favoriteServers.contains(s2.serverIP);
                if (f1 && !f2) return -1;
                if (!f1 && f2) return 1;

                return 0;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers();
        this.btnSelectServer.enabled = hasSelection;
        this.btnEditServer.enabled = hasSelection;
        this.btnDeleteServer.enabled = hasSelection;
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
                this.isDirectConnecting = true;
                this.currentServerData = new ServerData("Servidor de Minecraft", "", false);
                this.mc.displayGuiScreen(new GuiScreenServerList(this, this.currentServerData));
                break;
            case 3:
                if (this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers()) {
                    String name = this.savedServerList.getServerData(this.selectedServer).serverName;
                    this.isDeletingServer = true;
                    GuiYesNo guiyesno = new GuiYesNo(this, "Deletar servidor?", "Deseja remover '" + name + "'?", "Sim", "Cancelar", this.selectedServer);
                    this.mc.displayGuiScreen(guiyesno);
                }
                break;
            case 4:
                if (this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers()) {
                    this.isEditingServer = true;
                    this.currentServerData = this.savedServerList.getServerData(this.selectedServer);
                    this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.currentServerData));
                }
                break;
            case 7:
                this.mc.displayGuiScreen(new GuiMultiplayerCustom(this.parentScreen));
                break;
            case 8:
                this.isAddingServer = true;
                this.currentServerData = new ServerData("Servidor de Minecraft", "", false);
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.currentServerData));
                break;
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (this.isDirectConnecting) {
            this.isDirectConnecting = false;
            if (result) {
                this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.currentServerData));
            } else {
                this.mc.displayGuiScreen(this);
            }
            return;
        }

        if (this.isAddingServer) {
            this.isAddingServer = false;
            if (result) {
                this.savedServerList.addServerData(this.currentServerData);
                this.savedServerList.saveServerList();
            }
            this.mc.displayGuiScreen(this);
            return;
        }

        if (this.isEditingServer) {
            this.isEditingServer = false;
            if (result) {
                this.savedServerList.saveServerList();
            }
            this.mc.displayGuiScreen(this);
            return;
        }

        if (this.isDeletingServer) {
            this.isDeletingServer = false;
            if (result && id >= 0 && id < this.savedServerList.countServers()) {
                this.savedServerList.removeServerData(id);
                this.savedServerList.saveServerList();
                this.selectedServer = -1;
                organizeServerList();
            }
            this.mc.displayGuiScreen(this);
        }
    }

    private void connectToSelected() {
        if (this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers()) {
            this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.savedServerList.getServerData(this.selectedServer)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float progress = 1.0f;
        if (isOpening || isClosing) {
            long elapsedTime = System.currentTimeMillis() - this.animationStartTime;
            progress = Math.min(1.0f, (float)elapsedTime / (float)this.ANIMATION_DURATION_MS);
        }
        float easedProgress = AnimationUtil.easeOutCubic(progress);

        drawDefaultBackground();

        this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);

        float uiAlpha = (isOpening) ? easedProgress : (isClosing ? 1.0f - easedProgress : 1.0f);

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
    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, new Color(10, 10, 15, 120).getRGB());
    }

    private void triggerExitAnimation(GuiScreen screenToOpen) {
        if (!this.isClosing) {
            this.isClosing = true;
            this.isOpening = false;
            this.animationStartTime = System.currentTimeMillis();
            this.nextScreen = screenToOpen;
        }
    }

    private void drawSlidingBarsTransition(float progress) {
        if(progress <= 0.01f) return;
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
        float barHeight = (float)this.height / numBars;
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
        wr.pos(x, y, 0).color(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, c.getAlpha()/255f).endVertex();
    }

    private void startPingSystem() {
        if (this.pingTaskExecutor != null && !this.pingTaskExecutor.isShutdown()) {
            this.pingTaskExecutor.shutdownNow();
        }

        this.pingTaskExecutor = Executors.newFixedThreadPool(15);

        for (int i = 0; i < this.savedServerList.countServers(); i++) {
            final ServerData server = this.savedServerList.getServerData(i);

            server.pingToServer = -1L;
            server.serverMOTD = null;
            server.populationInfo = null;

            pingTaskExecutor.submit(() -> {
                OldServerPinger customPinger = new OldServerPinger();
                boolean received = false;

                try {
                    long startTime = System.currentTimeMillis();
                    customPinger.ping(server);

                    long timeout = 4000;

                    while (System.currentTimeMillis() - startTime < timeout) {
                        customPinger.pingPendingNetworks();

                        if (server.serverMOTD != null && server.serverMOTD.contains("Can't connect")) {
                            server.serverMOTD = null;
                        }

                        if (server.populationInfo != null) {
                            received = true;
                            break;
                        }

                        Thread.sleep(10);
                    }

                    if (received) {
                        long totalTime = System.currentTimeMillis() - startTime;
                        server.pingToServer = totalTime;
                        if (server.serverMOTD == null || server.serverMOTD.contains("Can't connect")) {
                            server.serverMOTD = EnumChatFormatting.GREEN + "Online";
                        }
                    } else {
                        server.pingToServer = -1L;
                        server.serverMOTD = EnumChatFormatting.DARK_RED + "Não foi possível conectar";
                    }

                    customPinger.clearPendingNetworks();

                } catch (Exception e) {
                    server.pingToServer = -1L;
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Offline";
                    try { customPinger.clearPendingNetworks(); } catch (Exception ignored) {}
                }
            });
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
        }

        if (this.pingTaskExecutor != null) {
            this.pingTaskExecutor.shutdownNow();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    class CustomServerList extends GuiSlot {
        private static final int SLOT_WIDTH = 305;

        public CustomServerList(Minecraft mcIn, int width, int height, int top, int bottom, int slotHeight) {
            super(mcIn, width, height, top, bottom, slotHeight);
        }

        @Override
        protected int getSize() {
            return savedServerList.countServers();
        }

        @Override
        protected int getScrollBarX() {
            return this.width / 2 + SLOT_WIDTH / 2 + 6;
        }

        @Override
        public int getListWidth() {
            return SLOT_WIDTH;
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            if (slotIndex >= savedServerList.countServers()) return;

            int listLeft = (this.width / 2) - (SLOT_WIDTH / 2);
            ServerData data = savedServerList.getServerData(slotIndex);

            int nameWidth = mc.fontRendererObj.getStringWidth(data.serverName);
            int starX = listLeft + 35 + nameWidth + 5;
            int starWidth = 10;

            int slotTop = this.top + 4 - this.getAmountScrolled() + (slotIndex * this.slotHeight) + this.headerPadding;
            boolean validY = mouseY >= slotTop && mouseY <= slotTop + this.slotHeight;

            if (mouseX >= starX && mouseX <= starX + starWidth && validY) {
                if (data.serverIP.equalsIgnoreCase("redeworth.com")) return;

                this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));

                if (favoriteServers.contains(data.serverIP)) {
                    favoriteServers.remove(data.serverIP);
                } else {
                    favoriteServers.add(data.serverIP);
                }
                saveFavorites();
                organizeServerList();
                return;
            }

            selectedServer = slotIndex;
            updateButtonStates();
            if (isDoubleClick) connectToSelected();
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedServer;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawSlot(int entryID, int x, int y, int slotHeight, int mouseXIn, int mouseYIn) {
            if (entryID >= savedServerList.countServers()) return;

            ServerData server = savedServerList.getServerData(entryID);
            if (server == null) return;

            boolean isSelected = entryID == selectedServer;

            drawRect(x, y, x + SLOT_WIDTH, y + 36, new Color(0, 0, 0, 120).getRGB());

            if (isSelected) {
                int gold = new Color(255, 215, 0).getRGB();
                drawHorizontalLine(x, x + SLOT_WIDTH - 1, y, gold);
                drawHorizontalLine(x, x + SLOT_WIDTH - 1, y + 35, gold);
                drawVerticalLine(x, y, y + 35, gold);
                drawVerticalLine(x + SLOT_WIDTH - 1, y, y + 35, gold);
            }

            String name = server.serverName != null ? server.serverName : "Minecraft Server";
            mc.fontRendererObj.drawString(name, x + 35, y + 3, 0xFFFFFF);

            boolean isFavorite = favoriteServers.contains(server.serverIP);
            boolean isRedeWorth = server.serverIP.equalsIgnoreCase("redeworth.com");
            String starIcon = isFavorite || isRedeWorth ? "★" : "☆";
            int starColor = isFavorite || isRedeWorth ? 0xFFD700 : 0x808080;

            int nameWidth = mc.fontRendererObj.getStringWidth(name);
            int starX = x + 35 + nameWidth + 5;

            if (mouseXIn >= starX && mouseXIn <= starX + 10 && mouseYIn >= y && mouseYIn <= y + 10 && !isRedeWorth) {
                starColor = 0xFFFFFF;
            }
            mc.fontRendererObj.drawString(starIcon, starX, y + 3, starColor);

            String pingString;
            int pingColor;

            if (server.pingToServer < 0) {
                pingString = "(?)";
                pingColor = 0x808080;
            } else {
                pingString = server.pingToServer + "ms";
                if (server.pingToServer < 150) pingColor = 0x00FF00;
                else if (server.pingToServer < 300) pingColor = 0xFFFF00;
                else pingColor = 0xFF0000;
            }

            int pingWidth = mc.fontRendererObj.getStringWidth(pingString);
            mc.fontRendererObj.drawString(pingString, x + SLOT_WIDTH - 5 - pingWidth, y + 3, pingColor);

            String popInfo = server.populationInfo != null ? server.populationInfo : "";
            int popWidth = mc.fontRendererObj.getStringWidth(popInfo);
            mc.fontRendererObj.drawString(popInfo, x + SLOT_WIDTH - 5 - popWidth, y + 14, 0xAAAAAA);

            String motd = server.serverMOTD;
            if (motd == null) {
                motd = EnumChatFormatting.GRAY + "Carregando...";
            } else if (server.pingToServer > 0 && motd.contains("Can't connect")) {
                motd = EnumChatFormatting.GREEN + "Online";
            }

            int maxTextWidth = SLOT_WIDTH - 38 - 5;

            List<String> lines = mc.fontRendererObj.listFormattedStringToWidth(motd, maxTextWidth);
            for (int i = 0; i < Math.min(lines.size(), 2); i++) {
                mc.fontRendererObj.drawString(lines.get(i), x + 35, y + 14 + (i * 10), 0x808080);
            }

            prepareServerIcon(server);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (server.getBase64EncodedIconData() != null) {
                drawServerIcon(x + 2, y + 2, server.getBase64EncodedIconData());
            } else {
                mc.getTextureManager().bindTexture(UNKNOWN_SERVER);
                Gui.drawModalRectWithCustomSizedTexture(x + 2, y + 2, 0, 0, 32, 32, 32, 32);
            }
        }

        private void drawServerIcon(int x, int y, String iconData) {
            if (iconData == null) return;
            ResourceLocation icon = new ResourceLocation("servers/" + iconData.hashCode() + "/icon");
            DynamicTexture texture = (DynamicTexture)mc.getTextureManager().getTexture(icon);

            if (texture == null) {
                try {
                    java.awt.image.BufferedImage img = net.minecraft.client.renderer.texture.TextureUtil.readBufferedImage(new java.io.ByteArrayInputStream(org.apache.commons.codec.binary.Base64.decodeBase64(iconData)));
                    texture = new DynamicTexture(img);
                    mc.getTextureManager().loadTexture(icon, texture);
                } catch (Exception e) { return; }
            }
            mc.getTextureManager().bindTexture(icon);
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 32, 32, 32, 32);
        }

        private void prepareServerIcon(ServerData server) {
            if (server.getBase64EncodedIconData() == null && !server.field_78841_f) {
                server.field_78841_f = true;
                server.setBase64EncodedIconData(null);
            }
        }
    }
}