package com.vitorxp.SkyBlockModVX.handlers;

import com.vitorxp.SkyBlockModVX.gui.GuiPlayerInspector;
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

    // --- VARIÁVEIS PARA CONTROLAR O ATRASO ---
    private static EntityPlayer playerToInspect = null;
    private static int ticksToWait = -1; // -1 significa que não estamos esperando

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

                // --- LÓGICA DE ATRASO INICIADA ---
                // Em vez de abrir a GUI diretamente, nós preparamos o cronômetro.

                // 1. Fecha qualquer GUI que esteja aberta no momento do clique.
                // Isso ajuda a "limpar o caminho" para a GUI do plugin aparecer primeiro.
                mc.displayGuiScreen(null);

                // 2. Define o jogador que queremos inspecionar.
                playerToInspect = targetPlayer;

                // 3. Define quantos ticks de jogo vamos esperar antes de abrir nossa GUI.
                // 20 ticks = 1 segundo. 5 ticks é um atraso rápido de 0.25 segundos.
                ticksToWait = 5;

                // Cancela o evento original para que a vara de blaze não faça mais nada.
                event.setCanceled(true);
            }
        }
    }

    // --- NOVO EVENTO PARA GERENCIAR O CRONÔMETRO ---
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        // Roda a lógica apenas no final de cada tick para evitar problemas
        if (event.phase == TickEvent.Phase.END) {

            // Se o cronômetro estiver ativo (maior que 0), diminui um.
            if (ticksToWait > 0) {
                ticksToWait--;
            }
            // Se o cronômetro chegar a 0, é hora de abrir nossa GUI.
            else if (ticksToWait == 0 && playerToInspect != null) {

                // Abre a GUI de inspeção
                mc.displayGuiScreen(new GuiPlayerInspector(playerToInspect));

                // Reseta as variáveis para o próximo uso
                ticksToWait = -1;
                playerToInspect = null;
            }
        }
    }
}