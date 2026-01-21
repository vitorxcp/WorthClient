package com.vitorxp.WorthClient.gui.button;

import com.vitorxp.WorthClient.gui.GuiClientMainMenu;
import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import com.vitorxp.WorthClient.gui.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.awt.Color;

public class GuiModernButton extends GuiButton {

    private final long animationDelay;
    private long startTime = -1;
    private float hoverFade = 0.0f;

    public GuiModernButton(int buttonId, int x, int y, int width, int height, String buttonText, long delay) {
        super(buttonId, x, y, width, height, buttonText);
        this.animationDelay = delay;
    }

    public GuiModernButton(int buttonId, int x, int y, String buttonText, long delay) {
        this(buttonId, x, y, 200, 25, buttonText, delay);
    }

    public GuiModernButton(int buttonId, int x, int y, int width, int height, String buttonText) {
        super(buttonId, x, y, width, height, buttonText);
        this.animationDelay = 0;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.drawButton(mc, mouseX, mouseY, 1.0f);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float globalAlpha) {
        if (!this.visible) return;

        if (this.startTime == -1) {
            this.startTime = System.currentTimeMillis();
        }

        float alpha = globalAlpha;
        float yOffset = 0;

        if (this.animationDelay > 0) {
            long elapsedTime = System.currentTimeMillis() - this.startTime - this.animationDelay;
            if (elapsedTime < 0) return;

            float entranceProgress = Math.min(1.0F, (float)elapsedTime / 600.0F);
            float easedEntrance = AnimationUtil.easeOutCubic(entranceProgress);
            alpha = easedEntrance * globalAlpha;
            yOffset = (1.0F - easedEntrance) * 15.0F;
        }

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition + yOffset && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height + yOffset;
        this.hoverFade = this.hovered ? Math.min(1.0F, this.hoverFade + 0.15F) : Math.max(0.0F, this.hoverFade - 0.15F);

        Color accent;
        try {
            GuiClientMainMenu.Theme theme = GuiClientMainMenu.currentTheme;
            if (theme == null) theme = GuiClientMainMenu.Theme.DARK;
            accent = theme.accentColor;
        } catch (Exception e) {
            accent = new Color(158, 96, 32);
        }

        int bgAlphaStart = 80;
        int bgAlphaEnd = 160;
        int currentBgAlpha = (int) (AnimationUtil.lerp(bgAlphaStart, bgAlphaEnd, hoverFade) * alpha);
        currentBgAlpha = Math.min(255, Math.max(0, currentBgAlpha));
        int backgroundColor = new Color(0, 0, 0, currentBgAlpha).getRGB();
        int borderAlpha = (int) (255 * hoverFade * alpha);
        borderAlpha = Math.min(255, Math.max(0, borderAlpha));
        int borderColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), borderAlpha).getRGB();

        Color textIdle = new Color(220, 220, 220);
        Color textHover = accent;

        int rText = (int) AnimationUtil.lerp(textIdle.getRed(), textHover.getRed(), hoverFade);
        int gText = (int) AnimationUtil.lerp(textIdle.getGreen(), textHover.getGreen(), hoverFade);
        int bText = (int) AnimationUtil.lerp(textIdle.getBlue(), textHover.getBlue(), hoverFade);
        int aText = (int) (255 * alpha);
        aText = Math.min(255, Math.max(4, aText));

        int finalTextColor = new Color(rText, gText, bText, aText).getRGB();

        // Renderização
        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition + yOffset, this.width, this.height, 4.0f, backgroundColor);

        if (borderAlpha > 1) {
            RenderUtil.drawRoundedOutline(this.xPosition, this.yPosition + yOffset, this.width, this.height, 4.0f, 1.0f, borderColor);
        }

        this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, (int)(this.yPosition + yOffset + (this.height - 8) / 2), finalTextColor);
    }
}