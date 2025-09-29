package com.vitorxp.SkyBlockModVX.gui.button;

import com.vitorxp.SkyBlockModVX.gui.utils.AnimationUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiIconButton extends GuiButton {

    private final ResourceLocation icon;
    private float hoverFade = 0.0f;

    public GuiIconButton(int buttonId, int x, int y, int width, int height, ResourceLocation icon) {
        super(buttonId, x, y, width, height, "");
        this.icon = icon;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            float hoverSpeed = 0.1F;
            this.hoverFade = this.hovered ? Math.min(1.0F, this.hoverFade + hoverSpeed) : Math.max(0.0F, this.hoverFade - hoverSpeed);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();

            // A cor fica mais clara no hover
            float brightness = AnimationUtil.lerp(0.8f, 1.0f, this.hoverFade);
            GlStateManager.color(brightness, brightness, brightness, 1.0F);

            mc.getTextureManager().bindTexture(this.icon);
            drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, 0, this.width, this.height, this.width, this.height);

            GlStateManager.popMatrix();
        }
    }
}