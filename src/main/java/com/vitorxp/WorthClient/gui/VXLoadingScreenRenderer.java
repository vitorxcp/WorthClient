package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import java.awt.*;

public class VXLoadingScreenRenderer {

    private static final ResourceLocation LOGO = new ResourceLocation("worthclient", "icons/icon.png");
    private final Minecraft mc;

    private float fade = 0f;

    public VXLoadingScreenRenderer(Minecraft mc) {
        this.mc = mc;
    }

    public void render(String text, float progress) {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        fade = Math.min(fade + 0.04f, 1f);
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, fade);

        Gui.drawRect(0, 0, w, h, new Color(12, 12, 12).getRGB());

        mc.getTextureManager().bindTexture(LOGO);
        Gui.drawModalRectWithCustomSizedTexture(
                w / 2 - 128, h / 2 - 80,
                0, 0,
                256, 256,
                256, 256
        );

        Gui.drawRect(w/2 - 120, h/2 + 60, w/2 + 120, h/2 + 75, 0xFF333333);
        Gui.drawRect(w/2 - 120, h/2 + 60, w/2 - 120 + (int)(240 * progress), h/2 + 75, 0xFF00AAFF);

        mc.fontRendererObj.drawString(
                text,
                w/2 - mc.fontRendererObj.getStringWidth(text)/2,
                h/2 + 85,
                0xFFFFFFFF
        );
    }
}
