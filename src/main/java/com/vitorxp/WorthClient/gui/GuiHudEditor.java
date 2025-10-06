package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.hud.HudElement;
import com.vitorxp.WorthClient.hud.HudPositionManager;
import com.vitorxp.WorthClient.utils.RenderUtil; // Usaremos nosso RenderUtil
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class GuiHudEditor extends GuiScreen {

    private static final ResourceLocation ICON_DELETE = new ResourceLocation("worthclient", "icons/delete-icon.png");
    private static final int PADDING = 0;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "§6WorthClient §f- §7Editor de HUD", this.width / 2, 10, 0xFFFFFF);

        for (HudElement element : com.vitorxp.WorthClient.WorthClient.hudManager.getElements()) {
            clampPositionToScreen(element);

            int elementWidth = element.getWidth();
            int elementHeight = element.getHeight();

            int boxX = element.x - PADDING;
            int boxY = element.y - PADDING;
            int boxWidth = elementWidth + PADDING * 2;
            int boxHeight = elementHeight + PADDING * 2;

            int bgColor = 0x64000000;
            int borderColor = 0x50FFFFFF;
            RenderUtil.drawRect(boxX, boxY, boxX + boxWidth, boxY + boxHeight, bgColor);

            element.render(null);
            element.renderPost(null);

            boolean hovered = isMouseOver(mouseX, mouseY, boxX, boxY, boxWidth, boxHeight);
            if (hovered) {
                drawIcon(boxX + boxWidth - 10, boxY - 6, ICON_DELETE);
            }
        }

        this.drawCenteredString(fontRendererObj, "Arraste os elementos para reposicionar", this.width / 2, this.height - 20, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (HudElement element : com.vitorxp.WorthClient.WorthClient.hudManager.getElements()) {
            int boxWidth = element.getWidth() + PADDING * 2;
            int boxHeight = element.getHeight() + PADDING * 2;

            if (isMouseOver(mouseX, mouseY, element.x - PADDING, element.y - PADDING, boxWidth, boxHeight)) {
                element.dragging = true;
                element.dragOffsetX = mouseX - element.x;
                element.dragOffsetY = mouseY - element.y;
                return;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (HudElement element : com.vitorxp.WorthClient.WorthClient.hudManager.getElements()) {
            if (element.dragging) {
                element.x = mouseX - element.dragOffsetX;
                element.y = mouseY - element.dragOffsetY;
            }
        }
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (HudElement element : com.vitorxp.WorthClient.WorthClient.hudManager.getElements()) {
            if (element.dragging) {
                element.dragging = false;
                HudPositionManager.savePosition(element);
            }
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void clampPositionToScreen(HudElement element) {
        int elementWidth = element.getWidth();
        int elementHeight = element.getHeight();

        int boxWidth = elementWidth + PADDING * 2;
        int boxHeight = elementHeight + PADDING * 2;

        element.x = Math.max(PADDING, Math.min(this.width - boxWidth + PADDING, element.x));
        element.y = Math.max(PADDING, Math.min(this.height - boxHeight + PADDING, element.y));
    }

    private boolean isMouseOver(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void drawIcon(int x, int y, ResourceLocation icon) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        mc.getTextureManager().bindTexture(icon);
        int iconSize = 8;
        drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }
}