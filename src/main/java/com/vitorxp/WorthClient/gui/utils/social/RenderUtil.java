package com.vitorxp.WorthClient.gui.utils.social;

import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import java.awt.Color;

public class RenderUtil {

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x2 = x + width;
        float y2 = y + height;

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f, f1, f2, f3);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + (Math.sin((i * 3.141592653589793D) / 180.0D) * radius * -1.0D), y + radius + (Math.cos((i * 3.141592653589793D) / 180.0D) * radius * -1.0D));
        }
        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + (Math.sin((i * 3.141592653589793D) / 180.0D) * radius * -1.0D), y2 - radius + (Math.cos((i * 3.141592653589793D) / 180.0D) * radius * -1.0D));
        }
        for (int i = 180; i <= 270; i += 3) {
            GL11.glVertex2d(x2 - radius + (Math.sin((i * 3.141592653589793D) / 180.0D) * radius * -1.0D), y2 - radius + (Math.cos((i * 3.141592653589793D) / 180.0D) * radius * -1.0D));
        }
        for (int i = 270; i <= 360; i += 3) {
            GL11.glVertex2d(x2 - radius + (Math.sin((i * 3.141592653589793D) / 180.0D) * radius * -1.0D), y + radius + (Math.cos((i * 3.141592653589793D) / 180.0D) * radius * -1.0D));
        }

        GL11.glEnd();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawCircle(float x, float y, float radius, int color) {
        drawRoundedRect(x - radius, y - radius, radius * 2, radius * 2, radius, color);
    }
}