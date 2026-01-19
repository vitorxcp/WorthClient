package com.vitorxp.WorthClient.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class ClientLogoRenderer {

    private static final ResourceLocation CLIENT_LOGO_LOC = new ResourceLocation("worthclient", "textures/gui/logo_client.png");

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        Minecraft mc = Minecraft.getMinecraft();
        GuiScreen screen = event.gui;

        if (mc.gameSettings.hideGUI || screen == null) return;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            mc.getTextureManager().bindTexture(CLIENT_LOGO_LOC);

            int logoH = 45;
            int logoW = (int) (logoH * (1640.0f / 664.0f));

            int logoX = screen.width - logoW - 10;
            int logoY = screen.height - logoH - 10;

            GuiScreen.drawModalRectWithCustomSizedTexture(logoX, logoY, 0, 0, logoW, logoH, logoW, logoH);

        } catch (Exception ignored) {
            String text = "WorthClient";
            int strWidth = mc.fontRendererObj.getStringWidth(text);
            mc.fontRendererObj.drawStringWithShadow(text, screen.width - strWidth - 5, screen.height - 12, 0xFFFFFF);
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}