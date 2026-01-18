package com.vitorxp.WorthClient.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import com.vitorxp.WorthClient.utils.SSLTrustBypasser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GuiMultiplayerCustom extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");

    private final GuiScreen parentScreen;
    private ServerList savedServerList;
    private CustomServerList serverListSelector;
    private int selectedServer = -1;

    private GuiButton btnSelectServer, btnEditServer, btnDeleteServer;

    private ExecutorService apiExecutor;
    private final Set<String> favoriteServers = new HashSet<>();
    private final File favoritesFile;

    private boolean isAddingServer = false;
    private boolean isEditingServer = false;
    private boolean isDirectConnecting = false;
    private boolean isDeletingServer = false;

    private ServerData currentServerData;

    private int containerWidth;
    private int containerHeight;
    private int containerX;
    private int containerY;

    public GuiMultiplayerCustom(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
        try { SSLTrustBypasser.install(); } catch (Exception ignored) {}
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

        this.isAddingServer = false;
        this.isEditingServer = false;
        this.isDirectConnecting = false;
        this.isDeletingServer = false;

        this.containerWidth = 270;
        this.containerHeight = Math.min(400, this.height - 120);
        this.containerX = (this.width - this.containerWidth) / 2;
        this.containerY = 40;

        organizeServerList();

        this.serverListSelector = new CustomServerList(this.mc, this.width, this.height,
                this.containerY + 10,
                this.containerY + this.containerHeight - 10,
                55);

        createButtons();
        loadServersViaApi();
    }

    private void loadFavorites() {
        if (!favoritesFile.exists()) return;
        favoriteServers.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(favoritesFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) favoriteServers.add(line.trim());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveFavorites() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(favoritesFile))) {
            for (String ip : favoriteServers) writer.println(ip);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void organizeServerList() {
        try {
            java.lang.reflect.Field serversField = null;
            try { serversField = ServerList.class.getDeclaredField("servers"); }
            catch (NoSuchFieldException e) {
                try { serversField = ServerList.class.getDeclaredField("field_78858_b"); } catch (Exception ignored) {}
            }

            if (serversField != null) {
                serversField.setAccessible(true);
                List<ServerData> originalList = (List<ServerData>) serversField.get(savedServerList);
                Collections.sort(originalList, (s1, s2) -> {
                    boolean r1 = s1.serverIP.equalsIgnoreCase("redeworth.com");
                    boolean r2 = s2.serverIP.equalsIgnoreCase("redeworth.com");
                    if (r1 && !r2) return -1; if (!r1 && r2) return 1;
                    boolean f1 = favoriteServers.contains(s1.serverIP);
                    boolean f2 = favoriteServers.contains(s2.serverIP);
                    if (f1 && !f2) return -1; if (!f1 && f2) return 1;
                    return 0;
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createButtons() {
        int btnWidth = 75;
        int btnSpacing = 10;
        int startY = this.containerY + this.containerHeight + 15;

        int totalWidthRow = (btnWidth * 3) + (btnSpacing * 2);
        int startX = (this.width - totalWidthRow) / 2;

        this.buttonList.add(this.btnSelectServer = new GuiModernButton(1, startX, startY, btnWidth, 20, "Entrar", 0L));
        this.buttonList.add(new GuiModernButton(2, startX + btnWidth + btnSpacing, startY, btnWidth, 20, "Conexão Direta", 100L));
        this.buttonList.add(new GuiModernButton(7, startX + (btnWidth + btnSpacing) * 2, startY, btnWidth, 20, "Atualizar", 200L));

        int row2Y = startY + 25;
        this.buttonList.add(new GuiModernButton(8, startX, row2Y, btnWidth, 20, "Adicionar", 300L));
        this.buttonList.add(this.btnEditServer = new GuiModernButton(4, startX + btnWidth + btnSpacing, row2Y, btnWidth, 20, "Editar", 400L));
        this.buttonList.add(this.btnDeleteServer = new GuiModernButton(3, startX + (btnWidth + btnSpacing) * 2, row2Y, btnWidth, 20, "Deletar", 500L));

        this.buttonList.add(new GuiModernButton(0, 20, 20, 60, 20, "Voltar", 600L));
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers();
        this.btnSelectServer.enabled = hasSelection;
        this.btnEditServer.enabled = hasSelection;

        if (hasSelection) {
            ServerData data = this.savedServerList.getServerData(this.selectedServer);
            this.btnDeleteServer.enabled = !data.serverIP.equalsIgnoreCase("redeworth.com");
        } else {
            this.btnDeleteServer.enabled = false;
        }
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x; float y1 = y; float x2 = x + width; float y2 = y + height;
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        drawCircleSector(x1 + radius, y1 + radius, radius, 180, 270);
        drawCircleSector(x2 - radius, y1 + radius, radius, 90, 180);
        drawCircleSector(x2 - radius, y2 - radius, radius, 0, 90);
        drawCircleSector(x1 + radius, y2 - radius, radius, 270, 360);

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x1 + radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x1, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1 + radius, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawCircleSector(float cx, float cy, float r, int startAngle, int endAngle) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = startAngle; i <= endAngle; i++) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }

    private void loadServersViaApi() {
        if (this.apiExecutor != null && !this.apiExecutor.isShutdown()) this.apiExecutor.shutdownNow();
        this.apiExecutor = Executors.newFixedThreadPool(30);
        for (int i = 0; i < this.savedServerList.countServers(); i++) {
            final ServerData server = this.savedServerList.getServerData(i);
            server.serverMOTD = EnumChatFormatting.GRAY + "Carregando...";
            server.pingToServer = -1;
            this.apiExecutor.submit(() -> {
                if (!queryPrimaryApi(server)) querySecondaryApi(server);
            });
        }
    }

    private boolean queryPrimaryApi(ServerData server) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.mcsrvstat.us/2/" + server.serverIP);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                if (json.has("online") && json.get("online").getAsBoolean()) {
                    updateServerDataFromJson(server, json, true);
                    return true;
                }
            }
        } catch (Exception ignored) {}
        finally { if (connection != null) connection.disconnect(); }
        return false;
    }

    private void querySecondaryApi(ServerData server) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://api.minetools.eu/ping/" + server.serverIP);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (connection.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();
                JsonObject json = new JsonParser().parse(response.toString()).getAsJsonObject();
                if (!json.has("error")) updateServerDataFromJson(server, json, false);
                else {
                    server.serverMOTD = EnumChatFormatting.DARK_RED + "Offline";
                    server.pingToServer = -1;
                }
            } else {
                server.serverMOTD = EnumChatFormatting.DARK_RED + "Offline";
                server.pingToServer = -1;
            }
        } catch (Exception e) {
            server.serverMOTD = EnumChatFormatting.DARK_RED + "Can't Connect";
            server.pingToServer = -1;
        } finally { if (connection != null) connection.disconnect(); }
    }

    private void updateServerDataFromJson(ServerData server, JsonObject json, boolean isPrimary) {
        try {
            server.pingToServer = 1;
            String motdText = "Online";
            if (isPrimary) {
                if (json.has("motd") && json.get("motd").isJsonObject()) {
                    JsonObject motdObj = json.getAsJsonObject("motd");
                    if (motdObj.has("raw")) {
                        JsonArray rawArr = motdObj.getAsJsonArray("raw");
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<rawArr.size(); i++) sb.append(rawArr.get(i).getAsString()).append(i < rawArr.size()-1 ? "\n" : "");
                        motdText = sb.toString();
                    } else if (motdObj.has("clean")) {
                        JsonArray cleanArr = motdObj.getAsJsonArray("clean");
                        StringBuilder sb = new StringBuilder();
                        for (int i=0; i<cleanArr.size(); i++) sb.append(cleanArr.get(i).getAsString()).append("\n");
                        motdText = sb.toString();
                    }
                }
            } else {
                if (json.has("description")) {
                    motdText = json.get("description").getAsString();
                    if (motdText.startsWith("{")) motdText = "Minecraft Server";
                }
            }
            server.serverMOTD = motdText.replace("&", "\u00A7");
            String popInfo = "?/?";
            if (json.has("players") && json.get("players").isJsonObject()) {
                JsonObject players = json.getAsJsonObject("players");
                int online = players.has("online") ? players.get("online").getAsInt() : 0;
                int max = players.has("max") ? players.get("max").getAsInt() : 0;
                popInfo = EnumChatFormatting.GRAY + "" + online + "/" + max;
            }
            server.populationInfo = popInfo;
            String iconKey = isPrimary ? "icon" : "favicon";
            if (json.has(iconKey) && !json.get(iconKey).isJsonNull()) {
                String base64 = json.get(iconKey).getAsString();
                if (base64.contains(",")) base64 = base64.split(",")[1];
                server.setBase64EncodedIconData(base64);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) return;
        switch (button.id) {
            case 0: this.mc.displayGuiScreen(this.parentScreen); break;
            case 1: connectToSelected(); break;
            case 2:
                this.isDirectConnecting = true;
                this.currentServerData = new ServerData("Servidor de Minecraft", "", false);
                this.mc.displayGuiScreen(new GuiScreenServerList(this, this.currentServerData));
                break;
            case 3:
                if (this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers()) {
                    String name = this.savedServerList.getServerData(this.selectedServer).serverName;
                    if (this.savedServerList.getServerData(this.selectedServer).serverIP.equalsIgnoreCase("redeworth.com")) return;

                    this.isDeletingServer = true;
                    this.mc.displayGuiScreen(new GuiYesNo(this, "Deletar servidor?", "Deseja remover '" + name + "'?", "Sim", "Cancelar", this.selectedServer));
                }
                break;
            case 4:
                if (this.selectedServer >= 0 && this.selectedServer < this.savedServerList.countServers()) {
                    this.isEditingServer = true;
                    this.currentServerData = this.savedServerList.getServerData(this.selectedServer);
                    this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.currentServerData));
                }
                break;
            case 7: this.mc.displayGuiScreen(new GuiMultiplayerCustom(this.parentScreen)); break;
            case 8:
                this.isAddingServer = true;
                this.currentServerData = new ServerData("Servidor de Minecraft", "", false);
                this.mc.displayGuiScreen(new GuiScreenAddServer(this, this.currentServerData));
                break;
        }
    }

    private void prepareForConnection() {
        if (this.mc.theWorld != null) {
            NotificationRenderer.send(NotificationRenderer.Type.INFO, "Desconectando do servidor atual...");

            NetHandlerPlayClient netHandler = this.mc.getNetHandler();
            if (netHandler != null) {
                netHandler.getNetworkManager().closeChannel(new ChatComponentText("Trocando de Servidor"));
            }
            System.out.println("Desconectado do Servidor.");
            this.mc.loadWorld(null);
        } else {
            NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Conectando ao servidor...");
        }
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (this.isDirectConnecting) {
            this.isDirectConnecting = false;

            if (result) {
                prepareForConnection();

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
            if (result) this.savedServerList.saveServerList();
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
            prepareForConnection();

            this.mc.displayGuiScreen(new GuiConnecting(this, this.mc, this.savedServerList.getServerData(this.selectedServer)));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawRoundedRect(containerX, containerY, containerWidth, containerHeight, 10, 0xD00F0F0F);
        this.serverListSelector.drawScreen(mouseX, mouseY, partialTicks);
        this.drawCenteredString(this.fontRendererObj, "Servidores", this.width / 2, this.containerY - 15, 0xFFFFFF);
        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, 1.0f);
            else button.drawButton(this.mc, mouseX, mouseY);
        }
    }

    @Override
    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, 0x64050505);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        if (this.apiExecutor != null) this.apiExecutor.shutdownNow();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.serverListSelector.handleMouseInput();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX >= containerX && mouseX <= containerX + containerWidth &&
                mouseY >= containerY + 10 && mouseY <= containerY + containerHeight - 10) {

            int scroll = this.serverListSelector.getAmountScrolled();
            int relY = mouseY - (containerY + 10) + scroll;
            int slotHeight = 55;
            int slotIdx = relY / slotHeight;

            if (slotIdx >= 0 && slotIdx < this.savedServerList.countServers()) {
                this.serverListSelector.elementClicked(slotIdx, false, mouseX, mouseY);
            }
        }
    }

    class CustomServerList extends GuiSlot {
        private final int SLOT_CONTENT_WIDTH;

        private long lastClickTime = 0;
        private int lastClickedSlot = -1;

        public CustomServerList(Minecraft mcIn, int width, int height, int top, int bottom, int slotHeight) {
            super(mcIn, width, height, top, bottom, slotHeight);
            this.SLOT_CONTENT_WIDTH = containerWidth - 20;
        }

        @Override protected int getSize() { return savedServerList.countServers(); }
        @Override protected int getScrollBarX() { return containerX + containerWidth - 8; }
        @Override public int getListWidth() { return containerWidth; }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            ScaledResolution sr = new ScaledResolution(mc);
            int scale = sr.getScaleFactor();
            int viewY = this.mc.displayHeight - (this.bottom * scale);
            int viewHeight = (this.bottom - this.top) * scale;
            if (viewHeight < 0) viewHeight = 0;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(containerX * scale, viewY, containerWidth * scale, viewHeight);

            int slotHeight = this.slotHeight;
            int numServers = this.getSize();
            int scroll = this.getAmountScrolled();

            for (int i = 0; i < numServers; i++) {
                int yPos = this.top + (i * slotHeight) - scroll; // Removed header padding reference
                int slotHeightWithPadding = slotHeight - 4;
                if (yPos > this.bottom || yPos + slotHeightWithPadding < this.top) continue;
                this.drawSlot(i, 0, yPos, slotHeight, mouseX, mouseY);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            int maxScroll = this.func_148135_f();
            if (maxScroll > 0) {
                int barHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                if (barHeight < 32) barHeight = 32;
                if (barHeight > this.bottom - this.top - 8) barHeight = this.bottom - this.top - 8;
                int barTop = (int) this.getAmountScrolled() * (this.bottom - this.top - barHeight) / maxScroll + this.top;
                if (barTop < this.top) barTop = this.top;
                int scrollX = this.getScrollBarX();
                Gui.drawRect(scrollX, barTop, scrollX + 3, barTop + barHeight, 0x80FFFFFF);
            }
        }

        @Override protected void drawContainerBackground(Tessellator tessellator) {}
        @Override protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) {}
        @Override protected void drawBackground() {}

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            if (slotIndex >= savedServerList.countServers()) return;

            long now = System.currentTimeMillis();
            if (slotIndex == lastClickedSlot && (now - lastClickTime) < 350) {
                connectToSelected();
                return;
            }
            lastClickTime = now;
            lastClickedSlot = slotIndex;

            int entryX = containerX + 10;
            ServerData data = savedServerList.getServerData(slotIndex);

            int nameWidth = mc.fontRendererObj.getStringWidth(data.serverName);
            int starX = entryX + 35 + nameWidth + 5;
            int starWidth = 12;

            int scroll = this.getAmountScrolled();
            int slotY = this.top + (slotIndex * this.slotHeight) - scroll;

            boolean validY = mouseY >= slotY && mouseY <= slotY + this.slotHeight;
            if (mouseX >= starX - 2 && mouseX <= starX + starWidth + 2 && validY) {
                if (data.serverIP.equalsIgnoreCase("redeworth.com")) return;
                this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                if (favoriteServers.contains(data.serverIP)) favoriteServers.remove(data.serverIP);
                else favoriteServers.add(data.serverIP);
                saveFavorites();
                organizeServerList();
                selectedServer = -1;
                updateButtonStates();
                return;
            }

            GuiMultiplayerCustom.this.selectedServer = slotIndex;
            updateButtonStates();
        }

        @Override protected boolean isSelected(int slotIndex) { return false; }

        @Override
        protected void drawSlot(int entryID, int x_unused, int y, int slotHeight, int mouseXIn, int mouseYIn) {
            if (entryID >= savedServerList.countServers()) return;
            ServerData server = savedServerList.getServerData(entryID);
            if (server == null) return;

            int x = containerX + 10;
            int width = SLOT_CONTENT_WIDTH;
            int height = slotHeight - 4;

            boolean isSelected = (entryID == GuiMultiplayerCustom.this.selectedServer);
            boolean isHovered = mouseXIn >= x && mouseXIn <= x + width && mouseYIn >= y && mouseYIn <= y + height;

            int bgColor = 0x60000000;
            if (isSelected) bgColor = 0x90606060; // Mais claro para mostrar seleção
            else if (isHovered) bgColor = 0x80303030;

            drawRoundedRect(x, y, width, height, 5, new Color(bgColor, true).getRGB());

            String name = server.serverName != null ? server.serverName : "Minecraft Server";
            mc.fontRendererObj.drawString(name, x + 45, y + 5, 0xFFFFFF);

            boolean isFavorite = favoriteServers.contains(server.serverIP);
            boolean isRedeWorth = server.serverIP.equalsIgnoreCase("redeworth.com");
            String starIcon = isFavorite || isRedeWorth ? "★" : "☆";
            int starColor = isFavorite || isRedeWorth ? 0xFFFFD700 : 0xFF808080;
            int nameWidth = mc.fontRendererObj.getStringWidth(name);
            int starX = x + 45 + nameWidth + 5;
            if (mouseXIn >= starX && mouseXIn <= starX + 12 && mouseYIn >= y && mouseYIn <= y + 15 && !isRedeWorth) {
                starColor = 0xFFFFFFFF;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(starX, y + 4, 0);
            GlStateManager.scale(1.2f, 1.2f, 1.0f);
            mc.fontRendererObj.drawString(starIcon, 0, 0, starColor);
            GlStateManager.popMatrix();

            String pingString = server.pingToServer < 0 ? "Offline" : "Online";
            int pingColor = server.pingToServer < 0 ? 0xFF5555 : 0x55FF55;
            int pingStrWidth = mc.fontRendererObj.getStringWidth(pingString);
            mc.fontRendererObj.drawString(pingString, x + width - 5 - pingStrWidth, y + 5, pingColor);

            String popInfo = server.populationInfo != null ? server.populationInfo : "0/0";
            int popWidth = mc.fontRendererObj.getStringWidth(popInfo);
            mc.fontRendererObj.drawString(popInfo, x + width - 5 - popWidth, y + 18, 0xAAAAAA);

            String motd = server.serverMOTD != null ? server.serverMOTD : EnumChatFormatting.GRAY + "Pinging...";
            List<String> formattedLines = mc.fontRendererObj.listFormattedStringToWidth(motd, width - 55);
            for (int i = 0; i < Math.min(formattedLines.size(), 2); i++) {
                mc.fontRendererObj.drawString(formattedLines.get(i), x + 45, y + 18 + (i * 10), 0xAAAAAA);
            }

            prepareServerIcon(server);
            GlStateManager.enableTexture2D();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (server.getBase64EncodedIconData() != null) drawServerIcon(x + 6, y + 6, server.getBase64EncodedIconData());
            else {
                mc.getTextureManager().bindTexture(UNKNOWN_SERVER);
                Gui.drawModalRectWithCustomSizedTexture(x + 6, y + 6, 0, 0, 32, 32, 32, 32);
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