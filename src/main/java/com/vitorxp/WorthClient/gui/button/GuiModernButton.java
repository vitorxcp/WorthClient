package com.vitorxp.WorthClient.gui.button;

import com.vitorxp.WorthClient.gui.utils.AnimationUtil;
import com.vitorxp.WorthClient.gui.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.awt.Color;

public class GuiModernButton extends GuiButton {

    private final long animationDelay;
    private long startTime = -1;
    private float hoverFade = 0.0f;

    private final int baseColor;
    private final int hoverColor;

    public GuiModernButton(int buttonId, int x, int y, int width, int height, String buttonText, long delay, int baseColor, int hoverColor) {
        super(buttonId, x, y, width, height, buttonText);
        this.animationDelay = delay;
        this.baseColor = baseColor;
        this.hoverColor = hoverColor;
    }

    public GuiModernButton(int buttonId, int x, int y, int width, int height, String buttonText, long delay) {
        this(buttonId, x, y, width, height, buttonText, delay,
                new Color(50, 50, 60, 150).getRGB(),
                new Color(130, 90, 220, 200).getRGB()
        );
    }

    public GuiModernButton(int buttonId, int x, int y, String buttonText, long delay) {
        this(buttonId, x, y, 200, 25, buttonText, delay);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.drawButton(mc, mouseX, mouseY, 1.0f);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, float globalAlpha) {
        if (this.startTime == -1) {
            this.startTime = System.currentTimeMillis();
        }

        long elapsedTime = System.currentTimeMillis() - this.startTime - this.animationDelay;
        if (elapsedTime < 0) return;

        float entranceAnimationProgress = Math.min(1.0F, (float)elapsedTime / 600.0F);
        float easedEntranceProgress = AnimationUtil.easeOutCubic(entranceAnimationProgress);

        float localAlpha = easedEntranceProgress;
        float yOffset = (1.0F - easedEntranceProgress) * 20.0F;
        float finalAlpha = localAlpha * globalAlpha;

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition + yOffset && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height + yOffset;
        float hoverSpeed = 0.1F;
        this.hoverFade = this.hovered ? Math.min(1.0F, this.hoverFade + hoverSpeed) : Math.max(0.0F, this.hoverFade - hoverSpeed);

        Color idleColor = new Color(this.baseColor, true);
        Color hoverColor = new Color(this.hoverColor, true);

        int r = (int) AnimationUtil.lerp(idleColor.getRed(), hoverColor.getRed(), hoverFade);
        int g = (int) AnimationUtil.lerp(idleColor.getGreen(), hoverColor.getGreen(), hoverFade);
        int b = (int) AnimationUtil.lerp(idleColor.getBlue(), hoverColor.getBlue(), hoverFade);

        int a = (int) (AnimationUtil.lerp(idleColor.getAlpha(), hoverColor.getAlpha(), hoverFade) * finalAlpha);

        a = Math.min(255, a);

        int finalColor = new Color(r,g,b,a).getRGB();

        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition + yOffset, this.width, this.height, 5.0f, finalColor);

        int textColor = new Color(1.0f, 1.0f, 1.0f, finalAlpha).getRGB();
        this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, (int)(this.yPosition + yOffset + (this.height - 8) / 2), textColor);
    }
}