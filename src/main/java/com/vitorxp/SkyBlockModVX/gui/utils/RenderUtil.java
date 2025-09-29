package com.vitorxp.SkyBlockModVX.gui.utils;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x2 = x + width;
        float y2 = y + height;

        float r = (color >> 24 & 0xFF) / 255.0F;
        float g = (color >> 16 & 0xFF) / 255.0F;
        float b = (color >> 8 & 0xFF) / 255.0F;
        float a = (color & 0xFF) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GL11.glColor4f(r, g, b, a);

        GL11.glBegin(GL11.GL_POLYGON);
        // Cantos
        for (int i = 0; i <= 90; i += 3) GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y + radius + Math.cos(Math.toRadians(i)) * -radius); // Top Left
        for (int i = 90; i <= 180; i += 3) GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y2 - radius + Math.cos(Math.toRadians(i)) * -radius); // Bottom Left
        for (int i = 0; i <= 90; i += 3) GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y2 - radius + Math.cos(Math.toRadians(i)) * radius); // Bottom Right
        for (int i = 90; i <= 180; i += 3) GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y + radius + Math.cos(Math.toRadians(i)) * radius); // Top Right
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

}