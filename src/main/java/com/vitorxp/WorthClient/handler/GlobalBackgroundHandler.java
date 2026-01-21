package com.vitorxp.WorthClient.handler;

import com.vitorxp.WorthClient.gui.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;

public class GlobalBackgroundHandler {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    @SubscribeEvent
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        GuiScreen gui = event.gui;

        if (gui == null) return;
        if (gui instanceof GuiClientMainMenu) return;
        if (gui instanceof GuiPauseMenuCustom) return;
        if (gui instanceof GuiModMenu) return;
        if (gui instanceof GuiHudEditor) return;
        if (gui instanceof GuiAutoLoginConfig) return;
        if (gui instanceof GuiAutoLoginServers) return;
        if (gui instanceof GuiEditorAdmin) return;
        if (gui instanceof GuiContainer) return;
        if (gui instanceof GuiChat) return;

        drawCustomBackground(gui.width, gui.height);
    }

    private void drawCustomBackground(int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        drawModalRectWithCustomSizedTexture(0, 0, 0, 0, width, height, width, height);

        Color themeColor;
        if (GuiClientMainMenu.currentTheme != null) {
            themeColor = GuiClientMainMenu.currentTheme.overlayColor;
        } else {
            themeColor = new Color(15, 15, 15, 180);
        }

        drawRect(0, 0, width, height, themeColor.getRGB());
    }

    public static void drawModalRectWithCustomSizedTexture(int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(u * f, (v + (float)height) * f1).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex((u + (float)width) * f, (v + (float)height) * f1).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex((u + (float)width) * f, v * f1).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(u * f, v * f1).endVertex();
        tessellator.draw();
    }

    public static void drawRect(int left, int top, int right, int bottom, int color) {
        if (left < right) { int i = left; left = right; right = i; }
        if (top < bottom) { int j = top; top = bottom; bottom = j; }

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}