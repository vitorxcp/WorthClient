package com.vitorxp.SkyBlockModVX.handlers;

import com.vitorxp.SkyBlockModVX.RadarManager;
import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.gui.AdminGui;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

import java.awt.Rectangle;

public class RadarInteractionHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onMouseClick(MouseEvent event) {
        if (SkyBlockMod.RadarOverlay && event.button == 0 && event.buttonstate && mc.currentScreen == null) {

            int mouseX = Mouse.getX() * mc.displayWidth / mc.displayWidth;
            int mouseY = mc.displayHeight - Mouse.getY() * mc.displayHeight / mc.displayHeight - 1;

            for (String playerName : RadarManager.playerClickAreas.keySet()) {
                Rectangle area = RadarManager.playerClickAreas.get(playerName);

                if (area.contains(mouseX, mouseY)) {
                    mc.addScheduledTask(() -> mc.displayGuiScreen(new AdminGui(playerName)));

                    event.setCanceled(true);

                    break;
                }
            }
        }
    }
}