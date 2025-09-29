// Pacote: com.vitorxp.SkyBlockModVX.gui.button
package com.vitorxp.SkyBlockModVX.gui.button;

import com.vitorxp.SkyBlockModVX.gui.utils.AnimationUtil;
import com.vitorxp.SkyBlockModVX.gui.utils.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import java.awt.Color;

public class GuiModernButton extends GuiButton {

    private final long animationDelay;
    private long startTime = -1;
    private float hoverFade = 0.0f;

    // Construtor principal
    public GuiModernButton(int buttonId, int x, int y, String buttonText, long delay) {
        this(buttonId, x, y, 200, 25, buttonText, delay);
    }

    // Construtor com tamanho customizado
    public GuiModernButton(int buttonId, int x, int y, int width, int height, String buttonText, long delay) {
        super(buttonId, x, y, width, height, buttonText);
        this.animationDelay = delay;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.startTime == -1) {
            this.startTime = System.currentTimeMillis();
        }

        long elapsedTime = System.currentTimeMillis() - this.startTime - this.animationDelay;
        if (elapsedTime < 0) return;

        // --- Animações ---
        float animationProgress = Math.min(1.0F, (float)elapsedTime / 600.0F);
        float easedProgress = AnimationUtil.easeOutCubic(animationProgress);

        float alpha = easedProgress;
        float yOffset = (1.0F - easedProgress) * 20.0F;

        this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition + yOffset && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height + yOffset;
        float hoverSpeed = 0.1F;
        this.hoverFade = this.hovered ? Math.min(1.0F, this.hoverFade + hoverSpeed) : Math.max(0.0F, this.hoverFade - hoverSpeed);

        Color idleColor = new Color(50, 50, 60, (int)(150 * alpha));
        Color hoverColor = new Color(130, 90, 220, (int)(200 * alpha));

        int r = (int) AnimationUtil.lerp(idleColor.getRed(), hoverColor.getRed(), hoverFade);
        int g = (int) AnimationUtil.lerp(idleColor.getGreen(), hoverColor.getGreen(), hoverFade);
        int b = (int) AnimationUtil.lerp(idleColor.getBlue(), hoverColor.getBlue(), hoverFade);
        int a = (int) AnimationUtil.lerp(idleColor.getAlpha(), hoverColor.getAlpha(), hoverFade);
        int finalColor = new Color(r,g,b,a).getRGB();

        RenderUtil.drawRoundedRect(this.xPosition, this.yPosition + yOffset, this.width, this.height, 5.0f, finalColor);

        int textColor = new Color(1.0f, 1.0f, 1.0f, alpha).getRGB();
        this.drawCenteredString(mc.fontRendererObj, this.displayString, this.xPosition + this.width / 2, (int)(this.yPosition + yOffset + (this.height - 8) / 2), textColor);
    }
}