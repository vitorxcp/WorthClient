package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class PingHUD extends HudElement {

    public static String messageDisplayHUDPing = "§bPing: §f0ms";

    public PingHUD() {
        super("PingHUD", 10, 10);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!SkyBlockMod.pingOverlay || mc.getNetHandler() == null || mc.thePlayer == null) return;

        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
        int ping = (info != null) ? info.getResponseTime() : 0;

        String display = "§bPing: §f" + ping + "ms";
        messageDisplayHUDPing = display;

        fontRenderer.drawStringWithShadow(display, this.x, this.y, 0xFFFFFF);
    }

    @Override
    public int getWidth() {
        return fontRenderer.getStringWidth(messageDisplayHUDPing);
    }

    @Override
    public int getHeight() {
        return fontRenderer.FONT_HEIGHT;
    }
}