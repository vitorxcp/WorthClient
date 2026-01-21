package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.awt.Color;
import java.io.IOException;
import java.util.function.Consumer;

public class GuiTransparencyPicker extends GuiScreen {

    private final GuiScreen parent;
    private final String title;
    private Color currentColor;
    private final Consumer<Color> onColorChange;

    private int sliderX, sliderY, sliderW, sliderH;
    private float alphaValue;
    private boolean dragging = false;

    public GuiTransparencyPicker(GuiScreen parent, String title, Color currentColor, Consumer<Color> onColorChange) {
        this.parent = parent;
        this.title = title;
        this.currentColor = currentColor;
        this.onColorChange = onColorChange;
        this.alphaValue = currentColor.getAlpha() / 255.0f;
    }

    @Override
    public void initGui() {
        int w = 200;
        int h = 120;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        this.sliderW = 160;
        this.sliderH = 10;
        this.sliderX = x + 20;
        this.sliderY = y + 50;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(0, 0, width, height, 0x80000000);

        int w = 200;
        int h = 120;
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        GuiModMenu.drawRoundedRect(x, y, w, h, 10, 0xFF151515);
        GuiModMenu.drawRoundedOutline(x, y, w, h, 10, 1.5f, 0xFF9E6020);

        drawCenteredString(fontRendererObj, title + " - Opacidade", width / 2, y + 15, 0xFFFFFFFF);

        if (dragging) {
            float val = (float)(mouseX - sliderX) / sliderW;
            alphaValue = Math.max(0.0f, Math.min(1.0f, val));
            updateColor();
        }

        GuiModMenu.drawRoundedRect(sliderX, sliderY, sliderW, sliderH, 4, 0xFF333333);

        int fillW = (int)(sliderW * alphaValue);
        if (fillW > 0) {
            GuiModMenu.drawGradientRoundedRect(sliderX, sliderY, fillW, sliderH, 4, 0xFFFFAA00, 0xFFFF5500);
        }

        int knobX = sliderX + fillW;
        GuiModMenu.drawCircleSector(knobX, sliderY + 5, 6, 0, 360);
        String pct = String.format("%d%%", (int)(alphaValue * 100));
        drawCenteredString(fontRendererObj, pct, width / 2, sliderY + 20, 0xFFAAAAAA);

        int btnW = 80;
        int btnX = x + (w - btnW) / 2;
        int btnY = y + h - 35;
        boolean hover = mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + 20;
        GuiModMenu.drawRoundedRect(btnX, btnY, btnW, 20, 5, hover ? 0xFF2ECC71 : 0xFF27AE60);
        drawCenteredString(fontRendererObj, "CONCLUÍDO", btnX + btnW/2, btnY + 6, -1);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateColor() {
        int r = currentColor.getRed();
        int g = currentColor.getGreen();
        int b = currentColor.getBlue();
        int a = (int)(alphaValue * 255);
        this.currentColor = new Color(r, g, b, a);
        this.onColorChange.accept(this.currentColor);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            if (mouseX >= sliderX && mouseX <= sliderX + sliderW && mouseY >= sliderY - 5 && mouseY <= sliderY + sliderH + 5) {
                dragging = true;
            }

            int w = 200; int h = 120;
            int x = (this.width - w) / 2; int y = (this.height - h) / 2;
            int btnW = 80; int btnX = x + (w - btnW) / 2; int btnY = y + h - 35;

            if (mouseX >= btnX && mouseX <= btnX + btnW && mouseY >= btnY && mouseY <= btnY + 20) {
                mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
                mc.displayGuiScreen(parent);
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        dragging = false;
    }
}