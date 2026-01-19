package com.vitorxp.WorthClient.hud;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public abstract class SingleItemHUD extends HudElement {

    public SingleItemHUD(String id, int x, int y) {
        super(id, x, y);
    }

    protected abstract ItemStack getItemToRender();
    protected abstract boolean isEnabled();
    protected abstract String getStyle();

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (event != null && event.type != RenderGameOverlayEvent.ElementType.TEXT) return;

        if (!WorthClient.ArmorsOverlays || !isEnabled()) return;

        ItemStack itemStack = getItemToRender();

        if (event == null && itemStack == null) {
            itemStack = getDummyItem();
        }

        if (itemStack == null) return;

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();

        mc.getRenderItem().renderItemAndEffectIntoGUI(itemStack, this.x, this.y);

        String currentStyle = getStyle();

        if (currentStyle.equals("Padrão")) {
            mc.getRenderItem().renderItemOverlays(fontRenderer, itemStack, this.x, this.y);
        } else {
            renderCustomDurability(itemStack, currentStyle);
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();

        this.mc.getTextureManager().bindTexture(net.minecraft.client.gui.Gui.icons);
    }

    private ItemStack getDummyItem() {
        if (this.id.contains("Helmet")) return new ItemStack(Items.diamond_helmet);
        if (this.id.contains("Chest")) return new ItemStack(Items.diamond_chestplate);
        if (this.id.contains("Leggings")) return new ItemStack(Items.diamond_leggings);
        if (this.id.contains("Boots")) return new ItemStack(Items.diamond_boots);
        if (this.id.contains("Hand")) return new ItemStack(Items.iron_sword);
        return null;
    }

    private void renderCustomDurability(ItemStack item, String style) {
        String text = "";
        int color = 0xFFFFFFFF;

        if (item.isItemStackDamageable()) {
            int max = item.getMaxDamage();
            int current = item.getItemDamage();
            int remaining = max - current;

            float percent = (float) remaining / max;
            if (percent > 0.75f) color = 0xFF55FF55;
            else if (percent > 0.25f) color = 0xFFFFFF55;
            else color = 0xFFFF5555;

            if (style.equals("Valor")) {
                text = remaining + "/" + max;
            } else if (style.equals("Porcentagem")) {
                text = (int)(percent * 100) + "%";
            }
        } else if (item.stackSize > 1) {
            text = String.valueOf(item.stackSize);
        }

        if (!text.isEmpty()) {
            fontRenderer.drawStringWithShadow(text, this.x + 18, this.y + 4, color);
        }
    }

    @Override
    public int getWidth() {
        if (!getStyle().equals("Padrão")) return 60;
        return 16;
    }

    @Override
    public int getHeight() { return 16; }
}