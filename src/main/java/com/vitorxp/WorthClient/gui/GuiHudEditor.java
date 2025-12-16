package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.hud.HudElement;
import com.vitorxp.WorthClient.hud.HudPositionManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.io.IOException;

public class GuiHudEditor extends GuiScreen {

    private final Color themeColor = new Color(158, 96, 32);
    private final int selectionBorder = 0xFFFFAA00;
    private final int selectionFill = 0x40FFAA00;
    private static final int PADDING = 0;
    private static final int MIN_CLICK_SIZE = 12;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xCC000000, 0xCC150500);
        drawRect(width / 2 - 1, 0, width / 2 + 1, height, 0x15FFFFFF);
        drawRect(0, height / 2 - 1, width, height / 2 + 1, 0x15FFFFFF);
        drawCenteredString(fontRendererObj, "EDITOR DE HUD", width / 2, 20, themeColor.getRGB());
        drawCenteredString(fontRendererObj, "Arraste para mover", width / 2, 35, 0xFFAAAAAA);

        for (HudElement element : WorthClient.hudManager.getElements()) {
            GlStateManager.pushMatrix();
            element.render(null);
            element.renderPost(null);
            GlStateManager.popMatrix();

            int elemX = element.x;
            int elemY = element.y;
            int elemW = element.getWidth();
            int elemH = element.getHeight();

            boolean hovered = isMouseOver(mouseX, mouseY, element);
            boolean dragging = element.dragging;

            if (hovered || dragging) {
                GuiModMenu.drawRoundedRect(elemX, elemY, elemW, elemH, 2, selectionFill);
                GuiModMenu.drawRoundedOutline(elemX, elemY, elemW, elemH, 2, 1.5f, selectionBorder);

                if (dragging) {
                    String coords = "X: " + elemX + " Y: " + elemY;
                    int strW = fontRendererObj.getStringWidth(coords);
                    drawRect(elemX, elemY - 12, elemX + strW + 4, elemY, 0x90000000);
                    fontRendererObj.drawString(coords, elemX + 2, elemY - 10, 0xFFFFFFFF);

                    if (Math.abs(elemX + elemW / 2 - width / 2) < 5)
                        drawRect(elemX + elemW / 2, 0, elemX + elemW / 2 + 1, height, 0xFFFF0000);
                    if (Math.abs(elemY + elemH / 2 - height / 2) < 5)
                        drawRect(0, elemY + elemH / 2, width, elemY + elemH / 2 + 1, 0xFFFF0000);

                    if (elemX == 0) drawRect(0, 0, 2, height, 0xFF00FF00);
                    if (elemX + elemW == width) drawRect(width - 2, 0, width, height, 0xFF00FF00);
                }
            } else {
                GuiModMenu.drawRoundedOutline(elemX, elemY, elemW, elemH, 2, 1.0f, 0x20FFFFFF);
            }
        }
        renderBottomButtons(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void renderBottomButtons(int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 100.0f);

        int modsBtnW = 100;
        int modsBtnH = 25;
        int modsBtnX = width / 2 - modsBtnW / 2;
        int modsBtnY = height / 2 - modsBtnH / 2;

        boolean hoverMods = mouseX >= modsBtnX && mouseX <= modsBtnX + modsBtnW && mouseY >= modsBtnY && mouseY <= modsBtnY + modsBtnH;
        GuiModMenu.drawRoundedRect(modsBtnX, modsBtnY, modsBtnW, modsBtnH, 6, hoverMods ? 0xFF555555 : 0xFF333333);
        GuiModMenu.drawRoundedOutline(modsBtnX, modsBtnY, modsBtnW, modsBtnH, 6, 1.5f, hoverMods ? 0xFFFFFFFF : 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "MODS", width / 2, modsBtnY + 9, 0xFFFFFFFF);

        int resetBtnSize = 25;
        int resetBtnX = modsBtnX + modsBtnW + 10;
        int resetBtnY = modsBtnY;

        boolean hoverReset = mouseX >= resetBtnX && mouseX <= resetBtnX + resetBtnSize && mouseY >= resetBtnY && mouseY <= resetBtnY + resetBtnSize;
        GuiModMenu.drawRoundedRect(resetBtnX, resetBtnY, resetBtnSize, resetBtnSize, 6, hoverReset ? 0xFFFF5555 : 0xFF333333);
        GuiModMenu.drawRoundedOutline(resetBtnX, resetBtnY, resetBtnSize, resetBtnSize, 6, 1.5f, hoverReset ? 0xFFFFFFFF : 0xFFAAAAAA);
        drawCenteredString(fontRendererObj, "\u21BB", resetBtnX + resetBtnSize / 2, resetBtnY + 8, 0xFFFFFFFF);

        if (hoverReset) {
            String tooltip = "Resetar Posições";
            int tw = fontRendererObj.getStringWidth(tooltip);
            drawRect(mouseX + 8, mouseY - 15, mouseX + 8 + tw + 4, mouseY - 2, 0xCC000000);
            fontRendererObj.drawString(tooltip, mouseX + 10, mouseY - 13, 0xFFFFAA00);
        }
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int modsBtnW = 100;
        int modsBtnH = 25;
        int modsBtnX = width / 2 - modsBtnW / 2;
        int modsBtnY = height / 2 - modsBtnH / 2;

        if (mouseButton == 0 && mouseX >= modsBtnX && mouseX <= modsBtnX + modsBtnW && mouseY >= modsBtnY && mouseY <= modsBtnY + modsBtnH) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            mc.displayGuiScreen(new GuiModMenu());
            return;
        }

        int resetBtnSize = 25;
        int resetBtnX = modsBtnX + modsBtnW + 10;
        int resetBtnY = modsBtnY;
        if (mouseButton == 0 && mouseX >= resetBtnX && mouseX <= resetBtnX + resetBtnSize && mouseY >= resetBtnY && mouseY <= resetBtnY + resetBtnSize) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            for (HudElement element : WorthClient.hudManager.getElements()) {
                element.x = 10;
                element.y = 10;
                HudPositionManager.savePosition(element);
            }
            return;
        }

        java.util.List<HudElement> elements = new java.util.ArrayList<>(WorthClient.hudManager.getElements());
        java.util.Collections.reverse(elements);

        for (HudElement element : elements) {
            if (isMouseOver(mouseX, mouseY, element)) {
                element.dragging = true;
                element.dragOffsetX = mouseX - element.x;
                element.dragOffsetY = mouseY - element.y;
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (HudElement element : WorthClient.hudManager.getElements()) {
            if (element.dragging) {
                element.x = mouseX - element.dragOffsetX;
                element.y = mouseY - element.dragOffsetY;

                int w = element.getWidth();
                int h = element.getHeight();

                if (Math.abs(element.x + w / 2 - width / 2) < 8) element.x = width / 2 - w / 2;
                if (Math.abs(element.y + h / 2 - height / 2) < 8) element.y = height / 2 - h / 2;

                if (element.x < 5) element.x = 0;
                if (element.y < 5) element.y = 0;
                if (element.x + w > width - 5) element.x = width - w;
                if (element.y + h > height - 5) element.y = height - h;

                element.x = Math.max(0, Math.min(width - w, element.x));
                element.y = Math.max(0, Math.min(height - h, element.y));
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (HudElement element : WorthClient.hudManager.getElements()) {
            if (element.dragging) {
                element.dragging = false;

                int w = element.getWidth();
                int h = element.getHeight();
                element.x = Math.max(0, Math.min(width - w, element.x));
                element.y = Math.max(0, Math.min(height - h, element.y));

                HudPositionManager.savePosition(element);
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    // Método corrigido para detectar o mouse melhor
    private boolean isMouseOver(int mouseX, int mouseY, HudElement element) {
        int x = element.x;
        int y = element.y;
        int w = element.getWidth();
        int h = element.getHeight();

        if (w < MIN_CLICK_SIZE) {
            x -= (MIN_CLICK_SIZE - w) / 2;
            w = MIN_CLICK_SIZE;
        }
        if (h < MIN_CLICK_SIZE) {
            y -= (MIN_CLICK_SIZE - h) / 2;
            h = MIN_CLICK_SIZE;
        }

        return mouseX >= x - PADDING && mouseX <= x + w + PADDING &&
                mouseY >= y - PADDING && mouseY <= y + h + PADDING;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}