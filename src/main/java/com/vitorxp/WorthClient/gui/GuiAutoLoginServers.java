package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.manager.AutoLoginManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;

public class GuiAutoLoginServers extends GuiScreen {

    private final GuiScreen parent;
    private final Color themeColor = new Color(158, 96, 32);
    private final int colBackgroundTop = 0xF0141414;
    private final int colBackgroundBottom = 0xF0230F05;

    private int guiWidth = 500;
    private int guiHeight = 350;
    private int guiLeft;
    private int guiTop;
    private float currentScale = 0.0f;
    private boolean closing = false;

    private float scrollOffset = 0;
    private float maxScroll = 0;

    public GuiAutoLoginServers(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        this.guiLeft = (this.width - this.guiWidth) / 2;
        this.guiTop = (this.height - this.guiHeight) / 2;
        this.currentScale = 0.0f;
        this.closing = false;
        this.scrollOffset = 0;
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getEventDWheel();
        if (dWheel != 0) {
            if (dWheel > 0) scrollOffset += 25;
            else scrollOffset -= 25;
            clampScroll();
        }
    }

    private void clampScroll() {
        if (scrollOffset > 0) scrollOffset = 0;
        if (scrollOffset < -maxScroll) scrollOffset = -maxScroll;
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

        drawRoundedRect(guiLeft, guiTop, guiWidth, guiHeight, 15, colBackgroundTop);
        drawRoundedOutline(guiLeft, guiTop, guiWidth, guiHeight, 15, 2.0f, themeColor.getRGB());

        drawCenteredString(fontRendererObj, "AutoLogin - Servidores", width / 2, guiTop + 20, themeColor.getRGB());

        boolean hoverBack = isHover(mouseX, mouseY, guiLeft + 20, guiTop + 20, 50, 20);
        drawRoundedRect(guiLeft + 20, guiTop + 20, 50, 20, 5, hoverBack ? 0xFF555555 : 0xFF333333);
        drawCenteredString(fontRendererObj, "< Voltar", guiLeft + 45, guiTop + 26, 0xFFFFFFFF);

        int addW = 120;
        int addX = guiLeft + guiWidth - addW - 20;
        int addY = guiTop + 20;
        boolean hoverAdd = isHover(mouseX, mouseY, addX, addY, addW, 20);

        drawRoundedRect(addX, addY, addW, 20, 5, hoverAdd ? 0xFF2ECC71 : 0xFF27AE60);
        drawCenteredString(fontRendererObj, "+ Novo Servidor", addX + addW / 2, addY + 6, 0xFFFFFFFF);

        drawRect(guiLeft + 20, guiTop + 50, guiLeft + guiWidth - 20, guiTop + 51, 0x40FFFFFF);

        int listStartY = guiTop + 60;
        int listHeight = guiHeight - 80;
        int itemHeight = 40;

        int totalHeight = AutoLoginManager.servers.size() * (itemHeight + 5);
        this.maxScroll = Math.max(0, totalHeight - listHeight);
        clampScroll();

        int scaleFactor = new net.minecraft.client.gui.ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((guiLeft + 20) * scaleFactor, (mc.displayHeight - (listStartY + listHeight) * scaleFactor), (guiWidth - 40) * scaleFactor, listHeight * scaleFactor);

        int currentY = (int) (listStartY + scrollOffset);

        if (AutoLoginManager.servers.isEmpty()) {
            drawCenteredString(fontRendererObj, "Nenhum servidor configurado.", width / 2, listStartY + 50, 0xFFAAAAAA);
        } else {
            for (int i = 0; i < AutoLoginManager.servers.size(); i++) {
                AutoLoginManager.ServerConfig cfg = AutoLoginManager.servers.get(i);

                if (currentY + itemHeight > listStartY && currentY < listStartY + listHeight) {
                    int itemX = guiLeft + 30;
                    int itemW = guiWidth - 60;
                    boolean hoverItem = isHover(mouseX, mouseY, itemX, currentY, itemW, itemHeight);

                    drawRoundedRect(itemX, currentY, itemW, itemHeight, 8, hoverItem ? 0xFF353535 : 0xFF252525);

                    // Ícone
                    drawCircleSector(itemX + 25, currentY + 20, 15, 0, 360, 0xFF555555); // Cinza fundo

                    GlStateManager.pushMatrix();
                    GlStateManager.scale(1.5, 1.5, 1);
                    String initial = cfg.serverIP.isEmpty() ? "?" : cfg.serverIP.substring(0, 1).toUpperCase();
                    drawCenteredString(fontRendererObj, initial, (int)((itemX + 25)/1.5), (int)((currentY + 15)/1.5), themeColor.getRGB());
                    GlStateManager.popMatrix();

                    fontRendererObj.drawStringWithShadow(cfg.serverIP, itemX + 50, currentY + 10, 0xFFFFFFFF);
                    fontRendererObj.drawString("Contas: " + cfg.accounts.size(), itemX + 50, currentY + 22, 0xFFAAAAAA);

                    drawCenteredString(fontRendererObj, "EDITAR >", itemX + itemW - 40, currentY + 16, hoverItem ? themeColor.getRGB() : 0xFF888888);
                }
                currentY += (itemHeight + 5);
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (isHover(mouseX, mouseY, guiLeft + 20, guiTop + 20, 50, 20)) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            closing = true;
            return;
        }

        int addW = 120;
        int addX = guiLeft + guiWidth - addW - 20;
        int addY = guiTop + 20;
        if (isHover(mouseX, mouseY, addX, addY, addW, 20)) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.displayGuiScreen(new GuiAutoLoginConfig(this, null));
            return;
        }

        int listStartY = guiTop + 60;
        int listHeight = guiHeight - 80;
        int itemHeight = 40;
        int currentY = (int) (listStartY + scrollOffset);

        if (mouseY >= listStartY && mouseY <= listStartY + listHeight) {
            for (int i = 0; i < AutoLoginManager.servers.size(); i++) {
                if (isHover(mouseX, mouseY, guiLeft + 30, currentY, guiWidth - 60, itemHeight)) {
                    mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                    AutoLoginManager.ServerConfig selected = AutoLoginManager.servers.get(i);
                    mc.displayGuiScreen(new GuiAutoLoginConfig(this, selected));
                    return;
                }
                currentY += (itemHeight + 5);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) closing = true;
    }

    private boolean isHover(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private float lerp(float a, float b, float f) { return a + f * (b - a); }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);

        drawCircleSector(x + radius, y + radius, radius, 180, 270, color);
        drawCircleSector(x + width - radius, y + radius, radius, 90, 180, color);
        drawCircleSector(x + width - radius, y + height - radius, radius, 0, 90, color);
        drawCircleSector(x + radius, y + height - radius, radius, 270, 360, color);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x + radius, y + height, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y + height, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y, 0.0D).endVertex();

        worldrenderer.pos(x, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + radius, y + radius, 0.0D).endVertex();
        worldrenderer.pos(x, y + radius, 0.0D).endVertex();

        worldrenderer.pos(x + width - radius, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + width, y + height - radius, 0.0D).endVertex();
        worldrenderer.pos(x + width, y + radius, 0.0D).endVertex();
        worldrenderer.pos(x + width - radius, y + radius, 0.0D).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 0; i <= 90; i += 10) worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 90; i <= 180; i += 10) worldrenderer.pos(x + width - radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 180; i <= 270; i += 10) worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + height - radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        for (int i = 270; i <= 360; i += 10) worldrenderer.pos(x + radius + Math.sin(Math.toRadians(i)) * radius, y + radius - Math.cos(Math.toRadians(i)) * radius, 0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircleSector(float cx, float cy, float r, int startAngle, int endAngle, int color) {
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.color(f1, f2, f3, f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = startAngle; i <= endAngle; i += 10) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }
}