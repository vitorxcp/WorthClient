package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class FPSHUD {

    public static String messageDisplayHUDFPS = "§bFPS: §f0";

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (SkyBlockMod.fpsOverlay) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null || mc.theWorld == null)
                return;

            HudElement element = HudPositionManager.get("FPSHUD");
            int x = element != null ? element.x : 10;
            int y = element != null ? element.y : 50;

            if (element == null) {
                HudPositionManager.registerElement(new HudElement("FPSHUD", 10, 50));
            }

            int height = 12;
            int padding = 4;
            int bgColor = new Color(0, 0, 0, 100).getRGB();
            int borderColor = new Color(255, 255, 255, 80).getRGB();

            int fps = Minecraft.getDebugFPS();

            String display = "§bFPS: §f" + fps;
            messageDisplayHUDFPS = display;

            int width = mc.fontRendererObj.getStringWidth(display);

            ScaledResolution res = new ScaledResolution(mc);
            //drawBorderedRect(x - padding, y - padding, x + width + padding, y + height + padding, 1.0F, borderColor, bgColor);

            mc.fontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
        }
    }

    public void drawBorderedRect(int x1, int y1, int x2, int y2, float borderSize, int borderColor, int insideColor) {
        drawRect(x1, y1, x2, y2, insideColor);
        drawRect(x1, y1, x2, (int) (y1 + borderSize), borderColor);
        drawRect(x1, (int) (y2 - borderSize), x2, y2, borderColor);
        drawRect(x1, (int) (y1 + borderSize), (int) (x1 + borderSize), (int) (y2 - borderSize), borderColor);
        drawRect((int) (x2 - borderSize), (int) (y1 + borderSize), x2, (int) (y2 - borderSize), borderColor);
    }

    public void drawRect(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}