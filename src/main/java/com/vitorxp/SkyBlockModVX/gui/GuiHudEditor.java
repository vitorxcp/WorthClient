package com.vitorxp.SkyBlockModVX.gui;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.hud.ArmorStatusHUD;
import com.vitorxp.SkyBlockModVX.hud.HudElement;
import com.vitorxp.SkyBlockModVX.hud.HudPositionManager;
import com.vitorxp.SkyBlockModVX.hud.KeystrokesHUD;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.vitorxp.SkyBlockModVX.hud.FPSHUD.messageDisplayHUDFPS;
import static com.vitorxp.SkyBlockModVX.hud.PetHud.messageDisplayHUDPet;
import static com.vitorxp.SkyBlockModVX.hud.PingHUD.messageDisplayHUDPing;

public class GuiHudEditor extends GuiScreen {

    private static final ResourceLocation ICON_DELETE = new ResourceLocation("skyblockmodvx", "textures/gui/icon_delete.png");

    private static final int PADDING = 4;
    private static final int HEIGHT = 12;

    private final Map<String, String> messageMap = new HashMap<String, String>() {{
        put("PingHUD", messageDisplayHUDPing);
        put("PetHUD", messageDisplayHUDPet);
        put("FPSHUD", messageDisplayHUDFPS);
    }};

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "§6SkyBlockModVX §f- §7Editor de HUD", this.width / 2, 10, 0xFFFFFF);

        for (HudElement element : HudPositionManager.elements.values()) {
            String display = messageMap.get(element.id);
            if (display != null) {
                int x = element.x;
                int y = element.y;
                int width = fontRendererObj.getStringWidth(display);
                int bgColor = 0x64000000;
                int borderColor = 0x50FFFFFF;

                drawBorderedRect(x - PADDING, y - PADDING, x + width + PADDING, y + HEIGHT + PADDING, 1.0F, borderColor, bgColor);
                mc.fontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
            }

            int boxWidth = 80;
            int boxHeight = 15;
            boolean hovered = isMouseOverElement(mouseX, mouseY, element.x, element.y, boxWidth, boxHeight);

            if (hovered) {
                drawIcon(element.x + boxWidth - 25, element.y, ICON_DELETE);
            }
        }

        int buttonWidth = 140;
        int buttonHeight = 40;
        int centerX = this.width / 2 - buttonWidth / 2;
        int centerY = this.height / 2 - buttonHeight / 2;

        drawRect(centerX + 2, centerY, centerX + buttonWidth - 2, centerY + buttonHeight, 0xAA000000);
        drawRect(centerX, centerY + 2, centerX + 2, centerY + buttonHeight - 2, 0xAA000000);
        drawRect(centerX + buttonWidth - 2, centerY + 2, centerX + buttonWidth, centerY + buttonHeight - 2, 0xAA000000);
        drawRect(centerX + 1, centerY + 1, centerX + buttonWidth - 1, centerY + 2, 0xAA000000);
        drawRect(centerX + 1, centerY + buttonHeight - 2, centerX + buttonWidth - 1, centerY + buttonHeight - 1, 0xAA000000);

        int textY = centerY + (buttonHeight - this.fontRendererObj.FONT_HEIGHT) / 2;
        drawCenteredString(this.fontRendererObj, "Menu de Configuração", this.width / 2, textY, 0xFFFFFF);

        HudPositionManager.save();

        ArmorStatusHUD.renderAllItemsHUD();
        KeystrokesHUD.renderAllItemsHUD();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private boolean isMouseOverElement(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void drawIcon(int x, int y, ResourceLocation icon) {
        mc.getTextureManager().bindTexture(icon);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); // Reset cor
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        drawTexturedModalRect(x, y, 0, 0, 16, 16);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        int buttonWidth = 120;
        int buttonHeight = 20;
        int centerX = this.width / 2 - buttonWidth / 2;
        int centerY = this.height / 2 - buttonHeight / 2;

        if (isMouseOverElement(x, y, centerX, centerY, buttonWidth, buttonHeight)) {
            mc.displayGuiScreen(null);
            SkyBlockMod.pendingOpenMenu = true;
            return;
        }

        for (HudElement element : HudPositionManager.elements.values()) {
            int boxWidth = 80;
            int boxHeight = 15;

            boolean hovered = isMouseOverElement(x, y, element.x, element.y, boxWidth, boxHeight);

            if (hovered) {
                if (x >= element.x + boxWidth - 15 && x <= element.x + boxWidth - 7) {
                    return;
                }
                if (x >= element.x + boxWidth - 28 && x <= element.x + boxWidth - 20) {
                    HudPositionManager.elements.remove(element.id);
                    if ("PingHUD".equals(element.id)) SkyBlockMod.pingOverlay = false;
                    if ("PetHUD".equals(element.id)) SkyBlockMod.petOverlay = false;
                    if ("FPSHUD".equals(element.id)) SkyBlockMod.fpsOverlay = false;
                    if ("MainHandHUD".equals(element.id)) SkyBlockMod.mainHandHUDOverlay = false;
                    if ("HelmetHUD".equals(element.id)) SkyBlockMod.helmetHUDOverlay = false;
                    if ("ChestplateHUD".equals(element.id)) SkyBlockMod.chestplateHUDOverlay = false;
                    if ("LeggingsHUD".equals(element.id)) SkyBlockMod.leggingsHUDOverlay = false;
                    if ("KeystrokesHUD".equals(element.id)) SkyBlockMod.keystrokesOverlay = false;
                    if ("KeystrokesLMB".equals(element.id)) SkyBlockMod.keystrokesOverlay = false;
                    if ("KeystrokesRMB".equals(element.id)) SkyBlockMod.keystrokesOverlay = false;
                    return;
                }
                element.dragging = true;
                element.dragOffsetX = x - element.x;
                element.dragOffsetY = y - element.y;
            }
        }
        HudPositionManager.save();
        super.mouseClicked(x, y, button);
    }

    @Override
    protected void mouseReleased(int x, int y, int state) {
        for (HudElement element : HudPositionManager.elements.values()) {
            element.dragging = false;
            //clampPositionToScreen(element);
        }
        HudPositionManager.save();
        super.mouseReleased(x, y, state);
    }

    @Override
    protected void mouseClickMove(int x, int y, int button, long timeSinceLastClick) {
        for (HudElement element : HudPositionManager.elements.values()) {
            if (element.dragging) {
                element.x = x - element.dragOffsetX;
                element.y = y - element.dragOffsetY;
            }
        }
        HudPositionManager.save();
        super.mouseClickMove(x, y, button, timeSinceLastClick);
    }

    private void clampPositionToScreen(HudElement element) {
        element.x = Math.max(0, Math.min(this.width - 80, element.x));
        element.y = Math.max(0, Math.min(this.height - 15, element.y));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    public void drawBorderedRect(int left, int top, int right, int bottom, float borderWidth, int borderColor, int backgroundColor) {
        drawRect(left, top, right, bottom, backgroundColor);
        drawHorizontalLine(left, right, top, borderColor);
        drawHorizontalLine(left, right, bottom, borderColor);
        drawVerticalLine(left, top, bottom, borderColor);
        drawVerticalLine(right, top, bottom, borderColor);
    }
}