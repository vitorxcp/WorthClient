package com.vitorxp.WorthClient.hud;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import java.util.ArrayList;
import java.util.List;

public class ArmorStatusHUD extends HudElement {

    private final boolean isVertical = true;

    public ArmorStatusHUD() {
        super("ArmorStatusHUD", 10, 70);
    }

    @Override
    public void renderPost(RenderGameOverlayEvent.Post event) {
        if (!shouldRender()) return;

        List<ItemStack> itemsToRender = getItems();

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        int currentX = this.x;
        int currentY = this.y;
        int itemSize = 16;

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

        this.mc.getTextureManager().bindTexture(net.minecraft.client.gui.Gui.icons);
    }

    private boolean shouldRender() {
        return com.vitorxp.WorthClient.WorthClient.mainHandHUDOverlay || com.vitorxp.WorthClient.WorthClient.helmetHUDOverlay || com.vitorxp.WorthClient.WorthClient.chestplateHUDOverlay ||
                com.vitorxp.WorthClient.WorthClient.leggingsHUDOverlay || com.vitorxp.WorthClient.WorthClient.bootsHUDOverlay;
    }

    private List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        if (!com.vitorxp.WorthClient.WorthClient.ArmorsOverlays) return items;
        if (com.vitorxp.WorthClient.WorthClient.helmetHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(3));
        if (com.vitorxp.WorthClient.WorthClient.chestplateHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(2));
        if (com.vitorxp.WorthClient.WorthClient.leggingsHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(1));
        if (com.vitorxp.WorthClient.WorthClient.bootsHUDOverlay) items.add(mc.thePlayer.getCurrentArmor(0));
        if (com.vitorxp.WorthClient.WorthClient.mainHandHUDOverlay) items.add(mc.thePlayer.getHeldItem());
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

    @Override
    public void render(RenderGameOverlayEvent event) {}
}