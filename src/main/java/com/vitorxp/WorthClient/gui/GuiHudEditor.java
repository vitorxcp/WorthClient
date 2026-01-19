package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.hud.HudElement;
import com.vitorxp.WorthClient.hud.HudPositionManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuiHudEditor extends GuiScreen {

    private final Color themeColor = new Color(158, 96, 32);
    private final int selectionBorder = 0xFFFFAA00;
    private final int selectionFill = 0x20FFAA00; // Fill bem transparente
    private final int multiSelectFill = 0x2000AAFF;
    private final int multiSelectBorder = 0x8000AAFF;
    private final Set<HudElement> selectedElements = new HashSet<>();
    private boolean isSelecting = false;
    private int selectStartX, selectStartY;
    private boolean showDisabled = false;

    @Override
    public void initGui() {
        selectedElements.clear();
        isSelecting = false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0xAA000000);
        drawCenteredString(fontRendererObj, "EDITOR DE HUD", width / 2, 20, themeColor.getRGB());

        for (HudElement element : WorthClient.hudManager.getElements()) {
            if (!isElementVisible(element) && !showDisabled) continue;

            GlStateManager.pushMatrix();
            element.render(null);
            element.renderPost(null);
            GlStateManager.popMatrix();

            drawElementBox(element, mouseX, mouseY);
        }

        if (isSelecting) {
            drawSelectionBox(selectStartX, selectStartY, mouseX, mouseY);
        }

        if (!selectedElements.isEmpty() && Mouse.isButtonDown(0)) {
            drawAlignmentLines();
        }

        renderBottomButtons(mouseX, mouseY);
    }

    private void drawElementBox(HudElement element, int mouseX, int mouseY) {
        int x = element.x;
        int y = element.y;
        int w = element.getWidth();
        int h = element.getHeight();

        boolean isSelected = selectedElements.contains(element);
        boolean isHovered = isMouseOver(mouseX, mouseY, element);

        if (isSelected) {
            GuiModMenu.drawRoundedRect(x - 2, y - 2, w + 4, h + 4, 4, selectionFill);
            GuiModMenu.drawRoundedOutline(x - 2, y - 2, w + 4, h + 4, 4, 1.5f, selectionBorder);

            if (selectedElements.size() == 1) {
                String coords = x + ", " + y;
                int strW = fontRendererObj.getStringWidth(coords);
                drawRect(x, y - 14, x + strW + 4, y - 2, 0xCC000000);
                fontRendererObj.drawString(coords, x + 2, y - 12, 0xFFFFFFFF);
            }
        } else if (isHovered) {
            GuiModMenu.drawRoundedOutline(x - 2, y - 2, w + 4, h + 4, 4, 1.0f, 0x40FFFFFF);
        } else if (showDisabled && !isElementVisible(element)) {
            GuiModMenu.drawRoundedOutline(x - 2, y - 2, w + 4, h + 4, 4, 1.0f, 0x40FF0000);
        }
    }

    private void drawAlignmentLines() {
        if (selectedElements.isEmpty()) return;

        HudElement ref = selectedElements.iterator().next();
        int snapDist = 4;
        int midX = ref.x + ref.getWidth() / 2;
        int midY = ref.y + ref.getHeight() / 2;

        if (Math.abs(midX - width / 2) < snapDist) drawRect(width / 2, 0, width / 2 + 1, height, 0xFFFF0000);
        if (Math.abs(midY - height / 2) < snapDist) drawRect(0, height / 2, width, height / 2 + 1, 0xFFFF0000);

        for (HudElement other : WorthClient.hudManager.getElements()) {
            if (selectedElements.contains(other) || (!isElementVisible(other) && !showDisabled)) continue;
            if (Math.abs(ref.x - other.x) < snapDist) drawRect(ref.x, 0, ref.x + 1, height, 0x80FF0000);
            if (Math.abs(ref.y - other.y) < snapDist) drawRect(0, ref.y, width, ref.y + 1, 0x80FF0000);
        }
    }

    private void drawSelectionBox(int x1, int y1, int x2, int y2) {
        int startX = Math.min(x1, x2);
        int startY = Math.min(y1, y2);
        int endX = Math.max(x1, x2);
        int endY = Math.max(y1, y2);
        GuiModMenu.drawRoundedRect(startX, startY, endX - startX, endY - startY, 0, multiSelectFill);
        GuiModMenu.drawRoundedOutline(startX, startY, endX - startX, endY - startY, 0, 1.0f, multiSelectBorder);
    }

    private void renderBottomButtons(int mouseX, int mouseY) {
        int btnW = 80;
        int btnH = 22;
        int spacing = 8;
        int smallBtnW = 22;
        int totalWidth = btnW + spacing + btnW + spacing + smallBtnW + spacing + smallBtnW;
        int startX = (width - totalWidth) / 2;
        int y = height - 40;

        drawButton(startX, y, btnW, btnH, "MODS", mouseX, mouseY, () -> mc.displayGuiScreen(new GuiModMenu()));
        drawButton(startX + btnW + spacing, y, btnW, btnH, "TECLAS", mouseX, mouseY, () -> mc.displayGuiScreen(new GuiKeybinds(this)));
        drawButton(startX + btnW * 2 + spacing * 2, y, smallBtnW, btnH, "\u21BB", mouseX, mouseY, () -> {
            for (HudElement element : WorthClient.hudManager.getElements()) {
                element.x = 10; element.y = 10;
                HudPositionManager.savePosition(element);
            }
        });

        int eyeX = startX + btnW * 2 + spacing * 3 + smallBtnW;
        String eyeIcon = showDisabled ? "O" : "Ø";
        drawButton(eyeX, y, smallBtnW, btnH, eyeIcon, mouseX, mouseY, () -> showDisabled = !showDisabled);
    }

    private void drawButton(int x, int y, int w, int h, String text, int mouseX, int mouseY, Runnable action) {
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        GuiModMenu.drawRoundedRect(x, y, w, h, 6, hover ? 0xFF404040 : 0xFF202020);
        GuiModMenu.drawRoundedOutline(x, y, w, h, 6, 1.0f, hover ? 0xFFFFFFFF : 0xFF606060);
        drawCenteredString(fontRendererObj, text, x + w / 2, y + 7, hover ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(null);
            return;
        }
        if (!selectedElements.isEmpty()) {
            int amount = isShiftKeyDown() ? 10 : 1;
            int dx = 0, dy = 0;
            if (keyCode == Keyboard.KEY_UP) dy = -amount;
            if (keyCode == Keyboard.KEY_DOWN) dy = amount;
            if (keyCode == Keyboard.KEY_LEFT) dx = -amount;
            if (keyCode == Keyboard.KEY_RIGHT) dx = amount;

            if (dx != 0 || dy != 0) {
                for (HudElement element : selectedElements) {
                    element.x = Math.max(0, Math.min(width - element.getWidth(), element.x + dx));
                    element.y = Math.max(0, Math.min(height - element.getHeight(), element.y + dy));
                    HudPositionManager.savePosition(element);
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int btnW = 80; int btnH = 22; int spacing = 8; int smallBtnW = 22;
        int totalWidth = btnW + spacing + btnW + spacing + smallBtnW + spacing + smallBtnW;
        int startX = (width - totalWidth) / 2;
        int y = height - 40;

        if (checkClick(startX, y, btnW, btnH, mouseX, mouseY)) { mc.displayGuiScreen(new GuiModMenu()); return; }
        if (checkClick(startX + btnW + spacing, y, btnW, btnH, mouseX, mouseY)) { mc.displayGuiScreen(new GuiKeybinds(this)); return; }
        if (checkClick(startX + btnW*2 + spacing*2, y, smallBtnW, btnH, mouseX, mouseY)) {
            for (HudElement e : WorthClient.hudManager.getElements()) { e.x=10; e.y=10; HudPositionManager.savePosition(e); } return;
        }
        if (checkClick(startX + btnW*2 + spacing*3 + smallBtnW, y, smallBtnW, btnH, mouseX, mouseY)) { showDisabled = !showDisabled; return; }

        boolean clickedElement = false;
        List<HudElement> reverseList = new ArrayList<>(WorthClient.hudManager.getElements());
        java.util.Collections.reverse(reverseList);

        for (HudElement element : reverseList) {
            if (!isElementVisible(element) && !showDisabled) continue;

            if (isMouseOver(mouseX, mouseY, element)) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    if (selectedElements.contains(element)) selectedElements.remove(element);
                    else selectedElements.add(element);
                } else {
                    if (!selectedElements.contains(element)) {
                        selectedElements.clear();
                        selectedElements.add(element);
                    }
                }
                for (HudElement sel : selectedElements) {
                    sel.dragging = true;
                    sel.dragOffsetX = mouseX - sel.x;
                    sel.dragOffsetY = mouseY - sel.y;
                }
                clickedElement = true;
                break;
            }
        }

        if (!clickedElement) {
            if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) selectedElements.clear();
            isSelecting = true;
            selectStartX = mouseX;
            selectStartY = mouseY;
        }
    }

    private boolean checkClick(int x, int y, int w, int h, int mx, int my) {
        if (mx >= x && mx <= x + w && my >= y && my <= y + h) {
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            return true;
        }
        return false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (isSelecting) return;

        for (HudElement element : selectedElements) {
            if (element.dragging) {
                int newX = mouseX - element.dragOffsetX;
                int newY = mouseY - element.dragOffsetY;

                if (selectedElements.size() == 1) {
                    int snapDist = 6;
                    int midX = newX + element.getWidth() / 2;
                    int midY = newY + element.getHeight() / 2;

                    if (Math.abs(midX - width / 2) < snapDist) newX = width / 2 - element.getWidth() / 2;
                    if (Math.abs(midY - height / 2) < snapDist) newY = height / 2 - element.getHeight() / 2;

                    for (HudElement other : WorthClient.hudManager.getElements()) {
                        if (other == element || selectedElements.contains(other)) continue;
                        if (!isElementVisible(other) && !showDisabled) continue;

                        if (Math.abs(newX - other.x) < snapDist) newX = other.x;
                        if (Math.abs(newY - other.y) < snapDist) newY = other.y;
                    }
                }

                newX = Math.max(0, Math.min(width - element.getWidth(), newX));
                newY = Math.max(0, Math.min(height - element.getHeight(), newY));

                element.x = newX;
                element.y = newY;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (isSelecting) {
            int startX = Math.min(selectStartX, mouseX);
            int startY = Math.min(selectStartY, mouseY);
            int endX = Math.max(selectStartX, mouseX);
            int endY = Math.max(selectStartY, mouseY);

            for (HudElement element : WorthClient.hudManager.getElements()) {
                if (!isElementVisible(element) && !showDisabled) continue;
                if (startX < element.x + element.getWidth() && endX > element.x &&
                        startY < element.y + element.getHeight() && endY > element.y) {
                    selectedElements.add(element);
                }
            }
            isSelecting = false;
        }

        for (HudElement element : WorthClient.hudManager.getElements()) {
            element.dragging = false;
            HudPositionManager.savePosition(element);
        }
    }

    private boolean isMouseOver(int mouseX, int mouseY, HudElement element) {
        return mouseX >= element.x && mouseX <= element.x + element.getWidth() &&
                mouseY >= element.y && mouseY <= element.y + element.getHeight();
    }

    private boolean isElementVisible(HudElement element) {
        String id = element.id;
        if (id.equals("FPSHUD")) return WorthClient.fpsOverlay;
        if (id.equals("PingHUD")) return WorthClient.pingOverlay;
        if (id.equals("PetHud")) return WorthClient.petOverlay;
        if (id.equals("RadarHUD")) return WorthClient.RadarOverlay;
        if (id.startsWith("Keystrokes")) return WorthClient.keystrokesOverlay;
        if (id.contains("Helmet") || id.contains("Chest") || id.contains("Leggings") ||
                id.contains("Boots") || id.contains("Hand")) return WorthClient.ArmorsOverlays;
        if (id.equals("ScoreboardHUD")) return com.vitorxp.WorthClient.hud.ScoreboardHUD.toggled;
        return true;
    }

    @Override
    public boolean doesGuiPauseGame() { return true; }
}