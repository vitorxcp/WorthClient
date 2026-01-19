package com.vitorxp.WorthClient.gui.utils;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

public class RenderUtil {

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x2 = x + width;
        float y2 = y + height;

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(r, g, b, a);

        GL11.glBegin(GL11.GL_POLYGON);
        for (int i = 0; i <= 90; i += 3)
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y + radius + Math.cos(Math.toRadians(i)) * -radius);
        for (int i = 90; i <= 180; i += 3)
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y2 - radius + Math.cos(Math.toRadians(i)) * -radius);
        for (int i = 0; i <= 90; i += 3)
            GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y2 - radius + Math.cos(Math.toRadians(i)) * radius);
        for (int i = 90; i <= 180; i += 3)
            GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y + radius + Math.cos(Math.toRadians(i)) * radius);
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float x2 = x + width;
        float y2 = y + height;

        float a = (float)(color >> 24 & 255) / 255.0F;
        float r = (float)(color >> 16 & 255) / 255.0F;
        float g = (float)(color >> 8 & 255) / 255.0F;
        float b = (float)(color & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(thickness);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int i = 0; i <= 90; i += 3)
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y + radius + Math.cos(Math.toRadians(i)) * -radius);

        for (int i = 90; i <= 180; i += 3)
            GL11.glVertex2d(x + radius + Math.sin(Math.toRadians(i)) * -radius, y2 - radius + Math.cos(Math.toRadians(i)) * -radius);

        for (int i = 0; i <= 90; i += 3)
            GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y2 - radius + Math.cos(Math.toRadians(i)) * radius);

        for (int i = 90; i <= 180; i += 3)
            GL11.glVertex2d(x2 - radius + Math.sin(Math.toRadians(i)) * radius, y + radius + Math.cos(Math.toRadians(i)) * radius);

        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}