package com.vitorxp.SkyBlockModVX.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

import java.io.IOException;
import java.util.Locale;

public class GuiPlayerInspector extends GuiScreen {

    private final EntityPlayer targetPlayer;

    public GuiPlayerInspector(EntityPlayer target) {
        this.targetPlayer = target;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        // Título
        this.drawCenteredString(this.fontRendererObj, "§eInspecionando: §f" + targetPlayer.getName(), centerX, startY, 0xFFFFFF);
        startY += 20;

        // --- Vida e Armadura ---
        float health = targetPlayer.getHealth();
        float maxHealth = targetPlayer.getMaxHealth();
        int armor = targetPlayer.getTotalArmorValue();
        String healthText = String.format(Locale.US, "§aVida: §f%.1f / %.1f", health, maxHealth);
        String armorText = String.format("§bArmadura: §f%d", armor);
        this.drawString(this.fontRendererObj, healthText, centerX - 90, startY, 0xFFFFFF);
        this.drawString(this.fontRendererObj, armorText, centerX + 10, startY, 0xFFFFFF);
        startY += 15;

        // --- Posição ---
        String posText = String.format("§6Posição: §fX: %d, Y: %d, Z: %d",
                MathHelper.floor_double(targetPlayer.posX),
                MathHelper.floor_double(targetPlayer.posY),
                MathHelper.floor_double(targetPlayer.posZ)
        );
        this.drawString(this.fontRendererObj, posText, centerX - 90, startY, 0xFFFFFF);
        startY += 25;

        // --- Equipamentos ---
        this.drawString(this.fontRendererObj, "§7Equipamentos:", centerX - 90, startY, 0xFFFFFF);
        startY += 10;

        // Pega os itens
        ItemStack heldItem = targetPlayer.getHeldItem();
        ItemStack[] armorItems = targetPlayer.inventory.armorInventory;

        // Prepara o OpenGL para renderizar itens
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();

        // Renderiza o item da mão
        int itemX = centerX - 90;
        this.mc.getRenderItem().renderItemAndEffectIntoGUI(heldItem, itemX, startY);
        this.mc.getRenderItem().renderItemOverlayIntoGUI(this.fontRendererObj, heldItem, itemX, startY, null);

        // Renderiza a armadura
        int armorX = centerX + 10;
        for (int i = 3; i >= 0; i--) { // Renderiza do capacete para as botas
            ItemStack armorPiece = armorItems[i];
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(armorPiece, armorX, startY);
            this.mc.getRenderItem().renderItemOverlayIntoGUI(this.fontRendererObj, armorPiece, armorX, startY, null);
            armorX += 20; // Espaçamento entre as peças
        }

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Fecha a GUI com a tecla ESC
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // Não pausa o jogo
    }
}