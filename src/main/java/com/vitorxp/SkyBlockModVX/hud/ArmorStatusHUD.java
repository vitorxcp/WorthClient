package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ArmorStatusHUD {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || mc.thePlayer == null || mc.theWorld == null) return;

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        if (SkyBlockMod.mainHandHUDOverlay) renderItemHUD("MainHandHUD", mc.thePlayer.getHeldItem());
        if (SkyBlockMod.helmetHUDOverlay) renderItemHUD("HelmetHUD", mc.thePlayer.getCurrentArmor(3));
        if (SkyBlockMod.chestplateHUDOverlay) renderItemHUD("ChestplateHUD", mc.thePlayer.getCurrentArmor(2));
        if (SkyBlockMod.leggingsHUDOverlay) renderItemHUD("LeggingsHUD", mc.thePlayer.getCurrentArmor(1));
        if (SkyBlockMod.bootsHUDOverlay) renderItemHUD("BootsHUD", mc.thePlayer.getCurrentArmor(0));

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    public static void renderAllItemsHUD() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        if (SkyBlockMod.mainHandHUDOverlay) renderItemHUD("MainHandHUD", mc.thePlayer.getHeldItem());
        if (SkyBlockMod.helmetHUDOverlay) renderItemHUD("HelmetHUD", mc.thePlayer.getCurrentArmor(3));
        if (SkyBlockMod.chestplateHUDOverlay) renderItemHUD("ChestplateHUD", mc.thePlayer.getCurrentArmor(2));
        if (SkyBlockMod.leggingsHUDOverlay) renderItemHUD("LeggingsHUD", mc.thePlayer.getCurrentArmor(1));
        if (SkyBlockMod.bootsHUDOverlay) renderItemHUD("BootsHUD", mc.thePlayer.getCurrentArmor(0));

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
    }

    private static void renderItemHUD(String id, ItemStack itemStack) {
        if (itemStack == null) return;

        HudElement element = HudPositionManager.get(id);
        if (element == null) {
            int defaultX = 10;
            int defaultY = 50;

            switch (id) {
                case "MainHandHUD":    defaultY = 50;  break;
                case "HelmetHUD":      defaultY = 70;  break;
                case "ChestplateHUD":  defaultY = 90;  break;
                case "LeggingsHUD":    defaultY = 110; break;
                case "BootsHUD":       defaultY = 130; break;
            }

            element = new HudElement(id, defaultX, defaultY);
            HudPositionManager.registerElement(element);
        }

        int x = element.x;
        int y = element.y;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(0.75f, 0.75f, 0.75f);
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, 0, 0);
        GlStateManager.popMatrix();
    }
}