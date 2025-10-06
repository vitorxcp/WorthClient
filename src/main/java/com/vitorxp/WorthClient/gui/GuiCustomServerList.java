package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.network.LanServerDetector;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumChatFormatting;
import java.awt.Color;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.gui.Gui.drawRect;

public class GuiCustomServerList extends GuiSlot {

    private final GuiCustomMultiplayer owner;
    private final Map<String, ServerIcon> serverIconCache = new HashMap<>();
    private ServerList serverList;
    private List<LanServerDetector.LanServer> lanServers = Collections.emptyList();

    public GuiCustomServerList(GuiCustomMultiplayer owner, Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.owner = owner;
    }

    public void setServerList(ServerList list) { this.serverList = list; }
    public void setLanServers(List<LanServerDetector.LanServer> list) { this.lanServers = list; }

    @Override
    protected int getSize() {
        int size = 0;
        if (this.serverList != null) size += this.serverList.countServers();
        if (this.lanServers != null) size += this.lanServers.size();
        return size;
    }

    public ServerData getServerData(int index) {
        if (this.serverList != null && index < this.serverList.countServers()) {
            return this.serverList.getServerData(index);
        }
        index -= (this.serverList != null ? this.serverList.countServers() : 0);
        if (this.lanServers != null && index < this.lanServers.size()) {
            LanServerDetector.LanServer lan = this.lanServers.get(index);
            return new ServerData(lan.getServerMotd(), lan.getServerIpPort(), true);
        }
        return null;
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        this.owner.selectServer(slotIndex);
        if (isDoubleClick) {
            this.owner.connectToSelected();
        }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
        return this.owner.isSelected(slotIndex);
    }

    @Override
    protected void drawBackground() {
        // Vazio
    }

    @Override
    protected void drawSlot(int slotIndex, int xPosition, int yPosition, int slotHeight, int mouseX, int mouseY) {
        final ServerData server = getServerData(slotIndex);
        if (server == null) return;

        // A LÓGICA DE PING FOI REMOVIDA DAQUI

        boolean isHovered = mouseX >= this.left && mouseX <= this.right && mouseY >= this.top && mouseY <= this.bottom && getSlotIndexFromScreenCoords(mouseX, mouseY) == slotIndex;

        int cardX = this.width / 2 - 200;
        int cardY = yPosition;
        int cardWidth = 400;
        int cardHeight = this.slotHeight - 4;

        // --- DESIGN DO CARD ---
        drawGradientRect(cardX, cardY, cardX + cardWidth, cardY + cardHeight, new Color(25, 25, 35, 100).getRGB(), new Color(35, 35, 45, 100).getRGB());

        if (isSelected(slotIndex)) RenderUtil.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 3.0f, new Color(140, 100, 255, 60).getRGB());
        else if (isHovered) RenderUtil.drawRoundedRect(cardX, cardY, cardWidth, cardHeight, 3.0f, new Color(255, 255, 255, 20).getRGB());

        String serverName = server.serverName != null ? server.serverName : "Servidor de Minecraft";
        String serverMotd = server.serverMOTD != null ? server.serverMOTD : "";
        this.mc.fontRendererObj.drawString(EnumChatFormatting.BOLD + serverName, cardX + 42, cardY + 8, 0xFFFFFF);
        this.mc.fontRendererObj.drawString(serverMotd, cardX + 42, cardY + 20, 0x909090);

        int rightAlignX = cardX + cardWidth - 8;
        String players = server.populationInfo != null ? server.populationInfo : "?";
        this.mc.fontRendererObj.drawString(players, rightAlignX - this.mc.fontRendererObj.getStringWidth(players), cardY + 8, 0xAAAAAA);

        Color statusColor;
        String statusText;
        if (server.pingToServer == -2L) { statusColor = new Color(255, 190, 0); statusText = "Buscando..."; }
        else if (server.pingToServer > 0) {
            long ping = server.pingToServer;
            if (ping < 100) statusColor = new Color(85, 255, 85);
            else if (ping < 200) statusColor = new Color(255, 255, 85);
            else statusColor = new Color(255, 85, 85);
            statusText = ping + "ms";
        } else { statusColor = new Color(170, 170, 170); statusText = "Offline"; }

        this.mc.fontRendererObj.drawString(statusText, rightAlignX - this.mc.fontRendererObj.getStringWidth(statusText), cardY + 20, statusColor.getRGB());

        drawRect(cardX, cardY, cardX + 4, cardY + cardHeight, statusColor.getRGB());

        drawServerIcon(server, cardX + 6, cardY + (cardHeight - 32) / 2);
    }

    private void drawServerIcon(ServerData server, int x, int y) {
        ServerIcon icon = serverIconCache.computeIfAbsent(server.serverIP, k -> new ServerIcon(server));
        icon.draw(x, y);
    }

    public void cleanup() {
        for (ServerIcon icon : serverIconCache.values()) {
            icon.cleanup();
        }
        serverIconCache.clear();
    }

    public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        float startAlpha = (float)(startColor >> 24 & 255) / 255.0F;
        float startRed   = (float)(startColor >> 16 & 255) / 255.0F;
        float startGreen = (float)(startColor >> 8 & 255) / 255.0F;
        float startBlue  = (float)(startColor & 255) / 255.0F;
        float endAlpha   = (float)(endColor >> 24 & 255) / 255.0F;
        float endRed     = (float)(endColor >> 16 & 255) / 255.0F;
        float endGreen   = (float)(endColor >> 8 & 255) / 255.0F;
        float endBlue    = (float)(endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)right, (double)top, 0.0D).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos((double)left, (double)top, 0.0D).color(startRed, startGreen, startBlue, startAlpha).endVertex();
        worldrenderer.pos((double)left, (double)bottom, 0.0D).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        worldrenderer.pos((double)right, (double)bottom, 0.0D).color(endRed, endGreen, endBlue, endAlpha).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
}