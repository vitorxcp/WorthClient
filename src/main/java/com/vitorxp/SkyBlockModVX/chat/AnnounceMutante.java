package com.vitorxp.SkyBlockModVX.chat;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.vitorxp.SkyBlockModVX.SkyBlockMod.announceZealot;

public class AnnounceMutante {
    private static boolean displayMessage = false;
    private static long displayUntilMU = 0;

    Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String raw = event.message.getUnformattedText();

        if (announceZealot && raw.equals("[The End] Um Enderman Mutante apareceu!")) {

            displayMessage = true;
            displayUntilMU = System.currentTimeMillis() + 5000;
            SkyBlockMod.zealotMessageTicksLeft = 100;

            Minecraft.getMinecraft().thePlayer.playSound("random.explode", 2.0F, 1.0F);
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (displayMessage && System.currentTimeMillis() <= displayUntilMU) {
            String message = "§c§lMUTANTE APARECEU!";
            int width = mc.fontRendererObj.getStringWidth(message);
            int x = (event.resolution.getScaledWidth() - width) / 2;
            int y = event.resolution.getScaledHeight() / 2 - 20;
            mc.fontRendererObj.drawStringWithShadow(message, x, y, 0xFF5555);
        } else {
            displayMessage = false;
        }
    }
}
