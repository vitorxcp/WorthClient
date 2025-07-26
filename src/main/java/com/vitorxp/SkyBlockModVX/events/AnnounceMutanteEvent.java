package com.vitorxp.SkyBlockModVX.events;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.zealotMessageTicksLeft;

public class AnnounceMutanteEvent {
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (zealotMessageTicksLeft > 0) {
            zealotMessageTicksLeft--;
            Minecraft.getMinecraft().ingameGUI.setRecordPlaying("§cUm Enderman Mutante apareceu!", false);
        }
    }
}
