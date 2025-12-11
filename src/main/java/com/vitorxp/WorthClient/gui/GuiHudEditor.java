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
    // Cores de Seleção
    private final int selectionBorder = 0xFFFFAA00;
    private final int selectionFill = 0x40FFAA00;
    private static final int PADDING = 2;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 0xCC000000, 0xCC150500);

        drawRect(width / 2 - 1, 0, width / 2 + 1, height, 0x40FFFFFF);
        drawRect(0, height / 2 - 1, width, height / 2 + 1, 0x40FFFFFF);

        drawCenteredString(fontRendererObj, "EDITOR DE HUD", width / 2, 20, themeColor.getRGB());
        drawCenteredString(fontRendererObj, "Arraste para mover", width / 2, 35, 0xFFAAAAAA);

        for (HudElement element : WorthClient.hudManager.getElements()) {
            clampPositionToScreen(element);

            GlStateManager.pushMatrix();
            element.render(null);
            element.renderPost(null);
            GlStateManager.popMatrix();

            int elemX = element.x;
            int elemY = element.y;
            int elemW = element.getWidth();
            int elemH = element.getHeight();

            boolean hovered = isMouseOver(mouseX, mouseY, elemX - PADDING, elemY - PADDING, elemW + PADDING*2, elemH + PADDING*2);
            boolean dragging = element.dragging;

            if (hovered || dragging) {
                GuiModMenu.drawRoundedRect(elemX - PADDING, elemY - PADDING, elemW + PADDING*2, elemH + PADDING*2, 4, selectionFill);
                GuiModMenu.drawRoundedOutline(elemX - PADDING, elemY - PADDING, elemW + PADDING*2, elemH + PADDING*2, 4, 1.5f, selectionBorder);

                if (dragging) {
                    String coords = "X: " + elemX + " Y: " + elemY;
                    int strW = fontRendererObj.getStringWidth(coords);
                    drawRect(elemX, elemY - 12, elemX + strW + 4, elemY, 0x80000000);
                    fontRendererObj.drawString(coords, elemX + 2, elemY - 10, 0xFFFFFFFF);

                    if (Math.abs(elemX + elemW/2 - width/2) < 5) drawRect(elemX + elemW/2, 0, elemX + elemW/2 + 1, height, 0xFFFF0000);
                    if (Math.abs(elemY + elemH/2 - height/2) < 5) drawRect(0, elemY + elemH/2, width, elemY + elemH/2 + 1, 0xFFFF0000);
                }
            } else {
                GuiModMenu.drawRoundedOutline(elemX - PADDING, elemY - PADDING, elemW + PADDING*2, elemH + PADDING*2, 4, 1.0f, 0x40FFFFFF);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 100.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        int modsBtnW = 100;
        int modsBtnH = 25;
        int modsBtnX = width / 2 - modsBtnW / 2;
        int modsBtnY = height / 2 - modsBtnH / 2;

        boolean hoverMods = mouseX >= modsBtnX && mouseX <= modsBtnX + modsBtnW && mouseY >= modsBtnY && mouseY <= modsBtnY + modsBtnH;

        int modsBg = hoverMods ? 0xFF555555 : 0xFF333333;
        int modsBorder = hoverMods ? 0xFFFFFFFF : 0xFFAAAAAA;

        GuiModMenu.drawRoundedRect(modsBtnX, modsBtnY, modsBtnW, modsBtnH, 6, modsBg);
        GuiModMenu.drawRoundedOutline(modsBtnX, modsBtnY, modsBtnW, modsBtnH, 6, 1.5f, modsBorder);
        drawCenteredString(fontRendererObj, "MODS", width / 2, modsBtnY + 9, 0xFFFFFFFF);

        GlStateManager.popMatrix();

        super.drawScreen(mouseX, mouseY, partialTicks);
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

        for (HudElement element : WorthClient.hudManager.getElements()) {
            int w = element.getWidth();
            int h = element.getHeight();

            if (isMouseOver(mouseX, mouseY, element.x - PADDING, element.y - PADDING, w + PADDING*2, h + PADDING*2)) {
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

                if (Math.abs(element.x + element.getWidth()/2 - width/2) < 8) {
                    element.x = width/2 - element.getWidth()/2;
                }
                if (Math.abs(element.y + element.getHeight()/2 - height/2) < 8) {
                    element.y = height/2 - element.getHeight()/2;
                }
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (HudElement element : WorthClient.hudManager.getElements()) {
            if (element.dragging) {
                element.dragging = false;
                HudPositionManager.savePosition(element);
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void clampPositionToScreen(HudElement element) {
        int w = element.getWidth();
        int h = element.getHeight();
        element.x = Math.max(0, Math.min(width - w, element.x));
        element.y = Math.max(0, Math.min(height - h, element.y));
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}