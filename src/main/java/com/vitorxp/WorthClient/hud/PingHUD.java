package com.vitorxp.WorthClient.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import org.lwjgl.opengl.GL11;
import java.awt.Color;

public class PingHUD extends HudElement {

    public static boolean enabled = true;
    public static float scale = 1.0f;
    public static boolean background = false;
    public static int backgroundColor = 0x80000000;
    public static boolean border = false;
    public static int borderColor = 0xFF000000;
    public static boolean chroma = false;
    public static int textColor = 0xFF00FFFF;
    public static boolean textShadow = true;

    public PingHUD() {
        super("PingHUD", 10, 10);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!enabled || mc.getNetHandler() == null || mc.thePlayer == null) return;

        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        int ping = (info != null) ? info.getResponseTime() : 0;
        String text = "Ping: " + ping + "ms";

        int colorToRender = chroma ? getRainbow(4, 0.8f, 1.0f) : textColor;

        GL11.glPushMatrix();

        GL11.glScalef(scale, scale, 1.0f);
        int xPos = (int) (this.x / scale);
        int yPos = (int) (this.y / scale);

        int width = fontRenderer.getStringWidth(text);
        int height = fontRenderer.FONT_HEIGHT;
        int padding = 2;

        if (border) {
            Gui.drawRect(xPos - padding - 1, yPos - padding - 1, xPos + width + padding + 1, yPos + height + padding + 1, borderColor);
        }

        if (background) {
            Gui.drawRect(xPos - padding, yPos - padding, xPos + width + padding, yPos + height + padding, backgroundColor);
        }

        if (textShadow) {
            fontRenderer.drawStringWithShadow(text, xPos, yPos, colorToRender);
        } else {
            fontRenderer.drawString(text, xPos, yPos, colorToRender);
        }

        GL11.glPopMatrix();
    }

    @Override
    public int getWidth() {
        String text = "Ping: 000ms";
        if (mc.thePlayer != null && mc.getNetHandler() != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            int ping = (info != null) ? info.getResponseTime() : 0;
            text = "Ping: " + ping + "ms";
        }
        return (int) ((fontRenderer.getStringWidth(text) + 4) * scale);
    }

    @Override
    public int getHeight() {
        return (int) ((fontRenderer.FONT_HEIGHT + 4) * scale);
    }

    public static int getRainbow(float seconds, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (int)(seconds * 1000)) / (float)(seconds * 1000);
        return Color.HSBtoRGB(hue, saturation, brightness);
    }
}