package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.config.KeystrokesColors;
import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Consumer;

public class GuiColorPicker extends GuiScreen {

    private final GuiScreen parent;
    private final String title;
    private Color currentColor;
    private final Consumer<Color> onColorChange;

    private int boxX, boxY, boxW, boxH;
    private int hueX, hueY, hueW, hueH;

    private float hue;
    private float saturation;
    private float brightness;

    private boolean draggingColor = false;
    private boolean draggingHue = false;

    public GuiColorPicker(GuiScreen parent, String title, Color currentColor, Consumer<Color> onColorChange) {
        this.parent = parent;
        this.title = title;
        this.currentColor = currentColor;
        this.onColorChange = onColorChange;

        float[] hsb = Color.RGBtoHSB(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public void initGui() {
        int windowWidth = 200;
        int windowHeight = 160;
        int guiLeft = (this.width - windowWidth) / 2;
        int guiTop = (this.height - windowHeight) / 2;

        this.boxW = 130;
        this.boxH = 100;
        this.boxX = guiLeft + 20;
        this.boxY = guiTop + 30;

        this.hueW = 15;
        this.hueH = 100;
        this.hueX = boxX + boxW + 15;
        this.hueY = boxY;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x80000000);

        int windowWidth = 200;
        int windowHeight = 160;
        int guiLeft = (this.width - windowWidth) / 2;
        int guiTop = (this.height - windowHeight) / 2;

        drawRoundedRect(guiLeft, guiTop, windowWidth, windowHeight, 10, 0xFF151515);
        drawRoundedOutline(guiLeft, guiTop, windowWidth, windowHeight, 10, 1.5f, 0xFF9E6020);

        drawCenteredString(fontRendererObj, title, width / 2, guiTop + 10, 0xFFFFFFFF);

        if (Mouse.isButtonDown(0)) {
            if (draggingColor) {
                saturation = Math.min(1.0f, Math.max(0.0f, (float)(mouseX - boxX) / boxW));
                brightness = Math.min(1.0f, Math.max(0.0f, 1.0f - (float)(mouseY - boxY) / boxH));
                updateColor();
            } else if (draggingHue) {
                hue = Math.min(1.0f, Math.max(0.0f, (float)(mouseY - hueY) / hueH));
                updateColor();
            }
        } else {
            draggingColor = false;
            draggingHue = false;
        }

        int hueColor = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        drawGradientRectHorizontal(boxX, boxY, boxW, boxH, 0xFFFFFFFF, hueColor);

        drawGradientRectVertical(boxX, boxY, boxW, boxH, 0x00000000, 0xFF000000);

        int selX = boxX + (int)(saturation * boxW);
        int selY = boxY + (int)((1.0f - brightness) * boxH);
        drawRoundedOutline(selX - 3, selY - 3, 6, 6, 3, 1.5f, 0xFFFFFFFF);
        drawRoundedOutline(selX - 4, selY - 4, 8, 8, 4, 1.0f, 0xFF000000);

        for (int i = 0; i < hueH; i++) {
            float h = (float) i / hueH;
            int c = Color.HSBtoRGB(h, 1.0f, 1.0f);
            drawRect(hueX, hueY + i, hueX + hueW, hueY + i + 1, c | 0xFF000000);
        }

        int hueIndY = hueY + (int)(hue * hueH);
        drawRect(hueX - 2, hueIndY - 1, hueX + hueW + 2, hueIndY + 2, 0xFFFFFFFF);

        int previewY = boxY + boxH + 10;
        int previewX = guiLeft + 20;

        drawRoundedRect(previewX, previewY, 20, 20, 5, currentColor.getRGB() | 0xFF000000);

        int btnW = 60;
        int btnX = guiLeft + windowWidth - btnW - 20;
        boolean hoverBtn = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= previewY && mouseY <= previewY + 20;
        drawRoundedRect(btnX, previewY, btnW, 20, 5, hoverBtn ? 0xFF2ECC71 : 0xFF27AE60);
        drawCenteredString(fontRendererObj, "Salvar", btnX + btnW/2, previewY + 6, 0xFFFFFFFF);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateColor() {
        this.currentColor = Color.getHSBColor(hue, saturation, brightness);
        this.onColorChange.accept(currentColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX >= boxX && mouseX <= boxX + boxW && mouseY >= boxY && mouseY <= boxY + boxH) {
            draggingColor = true;
        } else if (mouseX >= hueX && mouseX <= hueX + hueW && mouseY >= boxY && mouseY <= boxY + hueH) {
            draggingHue = true;
        }

        int previewY = boxY + boxH + 10;
        int btnW = 60;
        int btnX = (width - 200) / 2 + 180 - btnW;
        if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= previewY && mouseY <= previewY + 20) {
            mc.displayGuiScreen(parent);
            mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
            KeystrokesColors.saveColors();
            NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Configurações salvas!");
            com.vitorxp.WorthClient.manager.ConfigManager.save();
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        draggingColor = false;
        draggingHue = false;
    }

    public static void drawGradientRectHorizontal(int x, int y, int width, int height, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y + height, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(x + width, y + height, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(x + width, y, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawGradientRectVertical(int x, int y, int width, int height, int startColor, int endColor) {
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x + width, y, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(x, y, 0).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(x, y + height, 0).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(x + width, y + height, 0).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x;
        float y1 = y;
        float x2 = x + width;
        float y2 = y + height;
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        drawCircleSector(x1 + radius, y1 + radius, radius, 180, 270);
        drawCircleSector(x2 - radius, y1 + radius, radius, 90, 180);
        drawCircleSector(x2 - radius, y2 - radius, radius, 0, 90);
        drawCircleSector(x1 + radius, y2 - radius, radius, 270, 360);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float x1 = x;
        float y1 = y;
        float x2 = x + width;
        float y2 = y + height;
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        GL11.glLineWidth(thickness);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        for (int i = 270; i >= 180; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 180; i >= 90; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 90; i >= 0; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0).endVertex();
        }
        for (int i = 0; i >= -90; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0).endVertex();
        }
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawCircleSector(float cx, float cy, float r, int startAngle, int endAngle) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = startAngle; i <= endAngle; i += 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }
}