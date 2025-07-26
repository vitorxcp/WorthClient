package com.vitorxp.SkyBlockModVX.manager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class InventoryFullNotifier {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean warned = false;
    private static boolean displayMessage = false;
    private static long displayUntil = 0;

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || mc.theWorld == null) return;

        InventoryPlayer inv = mc.thePlayer.inventory;
        boolean isFull = true;

        for (int i = 0; i < inv.mainInventory.length; i++) {
            if (inv.mainInventory[i] == null) {
                isFull = false;
                break;
            }
        }

        if (isFull && !warned) {
            warned = true;
            displayMessage = true;
            displayUntil = System.currentTimeMillis() + 3000; // Mostrar por 3 segundos
            playSound();
        } else if (!isFull) {
            warned = false;
        }
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (displayMessage && System.currentTimeMillis() <= displayUntil) {
            String message = "§c§lINVENTÁRIO CHEIO!";
            int width = mc.fontRendererObj.getStringWidth(message);
            int x = (event.resolution.getScaledWidth() - width) / 2;
            int y = event.resolution.getScaledHeight() / 2 - 20;
            mc.fontRendererObj.drawStringWithShadow(message, x, y, 0xFF5555);
        } else {
            displayMessage = false;
        }
    }

    private void playSound() {
        mc.getSoundHandler().playSound(PositionedSoundRecord.create(
                new ResourceLocation("random.orb"), 1.0F));
    }
}