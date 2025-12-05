package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class VXLoadingScreen {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation logo = new ResourceLocation("worthclient", "icons/icon.png");

    private static float fakeProgress = 0f;

    public static void draw() {
        ScaledResolution sr = new ScaledResolution(mc);
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        drawRect(0, 0, w, h, 0xFF101010);

        mc.getTextureManager().bindTexture(logo);
        Gui.drawModalRectWithCustomSizedTexture(
                w / 2 - 128,
                h / 2 - 64,
                0, 0,
                256, 128,
                256, 128
        );

        drawRect(
                w / 2 - 100,
                h / 2 + 80,
                w / 2 + 100,
                h / 2 + 90,
                0xFF333333
        );

        drawRect(
                w / 2 - 100,
                h / 2 + 80,
                (int) (w / 2 - 100 + (200 * fakeProgress)),
                h / 2 + 90,
                0xFF00AAFF
        );

        mc.fontRendererObj.drawString(
                "Carregando SkyBlockModVX...",
                w / 2 - 80,
                h / 2 + 100,
                0xFFFFFFFF,
                false
        );

        GlStateManager.enableAlpha();
        GlStateManager.enableDepth();
    }

    public static void updateProgress() {
        fakeProgress += 0.004f;
        if (fakeProgress > 1f) fakeProgress = 1f;
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, bottom, color);
    }
}