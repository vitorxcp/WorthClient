package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import java.util.ArrayList;
import java.util.List;

public class ArmorStatusHUD extends HudElement {

    // Altere para false para ter um layout horizontal
    private final boolean isVertical = true;

    public ArmorStatusHUD() {
        super("ArmorStatusHUD", 10, 70);
    }

    // Este HUD precisa ser renderizado no evento "Post" para funcionar corretamente com os itens
    @Override
    public void renderPost(RenderGameOverlayEvent.Post event) {
        if (!shouldRender()) return;

        List<ItemStack> itemsToRender = getItems();

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        int currentX = this.x;
        int currentY = this.y;
        int itemSize = 16; // Tamanho do ícone do item

        for (ItemStack itemStack : itemsToRender) {
            if (itemStack != null) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, currentX, currentY);
                mc.getRenderItem().renderItemOverlays(fontRenderer, itemStack, currentX, currentY);

                if (isVertical) {
                    currentY += itemSize;
                } else {
                    currentX += itemSize;
                }
            }
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private boolean shouldRender() {
        // Verifica se alguma das opções de armadura está ativa
        return SkyBlockMod.mainHandHUDOverlay || SkyBlockMod.helmetHUDOverlay || SkyBlockMod.chestplateHUDOverlay ||
                SkyBlockMod.leggingsHUDOverlay || SkyBlockMod.bootsHUDOverlay;
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        if (SkyBlockMod.helmetHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(3));
        if (SkyBlockMod.chestplateHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(2));
        if (SkyBlockMod.leggingsHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(1));
        if (SkyBlockMod.bootsHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(0));
        if (SkyBlockMod.mainHandHUDOverlay) items.add(mc.thePlayer.getHeldItem());
        return items;
    }

    @Override
    public int getWidth() {
        int itemCount = getItems().size();
        return isVertical ? 16 : 16 * itemCount;
    }

    @Override
    public int getHeight() {
        int itemCount = getItems().size();
        return isVertical ? 16 * itemCount : 16;
    }

    // Deixamos este método vazio porque este HUD usa o `renderPost`
    @Override
    public void render(RenderGameOverlayEvent event) {}
}