package com.vitorxp.SkyBlockModVX.events;

import com.vitorxp.SkyBlockModVX.gui.GuiCustomMainMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class MenuInterceptor {
    private boolean pendingOpen = false;

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.gui instanceof GuiMainMenu && !(event.gui instanceof GuiCustomMainMenu)) {
            event.setCanceled(true);
            pendingOpen = true;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && pendingOpen) {
            pendingOpen = false;
            Minecraft.getMinecraft().displayGuiScreen(new GuiCustomMainMenu());
        }
    }
}