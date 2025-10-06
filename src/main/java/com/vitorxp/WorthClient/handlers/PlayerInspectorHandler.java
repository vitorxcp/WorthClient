package com.vitorxp.WorthClient.handlers;

import com.vitorxp.WorthClient.gui.GuiPlayerInspector;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PlayerInspectorHandler {

    private final Minecraft mc = Minecraft.getMinecraft();

    private static EntityPlayer playerToInspect = null;
    private static int ticksToWait = -1;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onPlayerEntityInteract(EntityInteractEvent event) {
        if (event.entityPlayer != mc.thePlayer) {
            return;
        }

        if (event.target instanceof EntityPlayer) {
            ItemStack heldItem = event.entityPlayer.getHeldItem();

            if (heldItem != null && heldItem.getItem() == Items.blaze_rod) {
                EntityPlayer targetPlayer = (EntityPlayer) event.target;

                mc.displayGuiScreen(null);

                playerToInspect = targetPlayer;

                ticksToWait = 5;

                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {

            if (ticksToWait > 0) {
                ticksToWait--;
            }
            else if (ticksToWait == 0 && playerToInspect != null) {

                mc.displayGuiScreen(new GuiPlayerInspector(playerToInspect));

                ticksToWait = -1;
                playerToInspect = null;
            }
        }
    }
}