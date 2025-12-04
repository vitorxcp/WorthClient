package com.vitorxp.WorthClient.hud;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class LookAtHUD extends HudElement {

    private final Minecraft mc = Minecraft.getMinecraft();

    // Dados para renderização
    private String name = "";
    private String modSource = "";
    private String extraInfo = "";
    private ItemStack mainPreviewStack = null;
    private final List<ItemStack> containerItems = new ArrayList<>();

    public LookAtHUD() {
        super("LookAtHUD", 10, 70);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) return;

        resetData();

        if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            handleBlockHit(mop);
        } else if (mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            handleEntityHit(mop);
        }

        if (!name.isEmpty()) {
            drawHudBox();
        }
    }

    private void resetData() {
        name = "";
        modSource = "";
        extraInfo = "";
        mainPreviewStack = null;
        containerItems.clear();
    }

    private void handleBlockHit(MovingObjectPosition mop) {
        BlockPos pos = mop.getBlockPos();
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block block = state.getBlock();

        if (block != null && block != net.minecraft.init.Blocks.air) {
            mainPreviewStack = block.getPickBlock(mop, mc.theWorld, pos, mc.thePlayer);
            if (mainPreviewStack == null) mainPreviewStack = new ItemStack(block);

            name = mainPreviewStack.getDisplayName();
            modSource = EnumChatFormatting.BLUE + "" + EnumChatFormatting.ITALIC + getModFromBlock(block);

            TileEntity te = mc.theWorld.getTileEntity(pos);
            if (te != null) {
                if (te instanceof IInventory) {
                    IInventory inv = (IInventory) te;
                    int maxItems = 9 * 3;
                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);
                        if (stack != null) {
                            containerItems.add(stack);
                        }
                        if (containerItems.size() >= maxItems) break;
                    }
                    if (!containerItems.isEmpty()) {
                        extraInfo = EnumChatFormatting.GRAY + "Conteúdo (" + containerItems.size() + ")";
                    }
                }
            }
        }
    }

    private void handleEntityHit(MovingObjectPosition mop) {
        Entity e = mop.entityHit;
        if (e != null) {
            if (e instanceof EntityArmorStand) {
                EntityArmorStand stand = (EntityArmorStand) e;
                ItemStack headItem = stand.getCurrentArmor(3);

                boolean isMinion = false;
                boolean isPet = false;
                String foundName = "";

                if (e.hasCustomName()) {
                    foundName = e.getCustomNameTag();
                }

                if (foundName.isEmpty() || foundName.equals("Armor Stand")) {
                    foundName = scanForHologramName(stand);
                }

                if (foundName.contains("Minion") || foundName.contains("Mithril") || foundName.contains("Collector")) {
                    isMinion = true;
                } else if (stand.isSmall() && headItem != null) {
                    isMinion = true;
                    if (foundName.isEmpty() || foundName.equals("Armor Stand")) {
                        foundName = EnumChatFormatting.YELLOW + "Minion";
                    }
                }

                if (foundName.contains("Pet")) {
                    isPet = true;
                }

                if (isMinion) {
                    name = foundName;
                    modSource = EnumChatFormatting.BLUE + "SkyBlock Minion";

                    if (headItem != null) {
                        mainPreviewStack = headItem.copy();
                    } else {
                        mainPreviewStack = new ItemStack(net.minecraft.init.Items.armor_stand);
                    }
                } else {
                    if (isPet) {
                        name = foundName;
                        modSource = EnumChatFormatting.BLUE + "SkyBlock Pet";

                        if (headItem != null) {
                            mainPreviewStack = headItem.copy();
                        } else {
                            mainPreviewStack = new ItemStack(net.minecraft.init.Items.armor_stand);
                        }
                    } else {
                        name = e.hasCustomName() ? e.getCustomNameTag() : EnumChatFormatting.WHITE + "Armor Stand";
                        modSource = EnumChatFormatting.BLUE + "Minecraft";
                        mainPreviewStack = new ItemStack(net.minecraft.init.Items.armor_stand);
                    }
                }

            } else if (e instanceof EntityPlayer) {
                name = EnumChatFormatting.GREEN + e.getName();
                extraInfo = EnumChatFormatting.RED + String.format("%.1f ❤", ((EntityPlayer) e).getHealth());
                mainPreviewStack = new ItemStack(net.minecraft.init.Items.skull, 1, 3);
                modSource = EnumChatFormatting.YELLOW + "Jogador";
            } else {
                name = e.getName();
                mainPreviewStack = new ItemStack(net.minecraft.init.Items.spawn_egg);
                modSource = EnumChatFormatting.GRAY + "Entidade";
            }
        }
    }

    private String scanForHologramName(Entity centerEntity) {
        AxisAlignedBB searchBox = centerEntity.getEntityBoundingBox().expand(0.5, 3.0, 0.5);

        List<EntityArmorStand> nearby = mc.theWorld.getEntitiesWithinAABB(EntityArmorStand.class, searchBox);

        for (EntityArmorStand nearbyStand : nearby) {
            if (nearbyStand.hasCustomName()) {
                String cleanName = nearbyStand.getCustomNameTag();
                String unformatted = EnumChatFormatting.getTextWithoutFormattingCodes(cleanName);

                if (unformatted.contains("Minion") || unformatted.contains("Collector")) {
                    return cleanName;
                }
            }
        }
        return "Armor Stand";
    }

    private void drawHudBox() {
        int iconSize = 28;
        int textWidth = Math.max(fontRenderer.getStringWidth(name), fontRenderer.getStringWidth(modSource));
        if (!extraInfo.isEmpty() && containerItems.isEmpty()) {
            textWidth = Math.max(textWidth, fontRenderer.getStringWidth(extraInfo));
        }

        int boxWidth = Math.max(140, textWidth + iconSize + 25);
        int headerHeight = 34;
        int inventoryHeight = 0;

        if (!containerItems.isEmpty()) {
            int itemsPerRow = 9;
            boxWidth = Math.max(boxWidth, (itemsPerRow * 18) + 10);
            int rows = (int) Math.ceil((double) containerItems.size() / itemsPerRow);
            inventoryHeight = (rows * 18) + 6;
        }

        int boxHeight = headerHeight + inventoryHeight;

        Gui.drawRect(this.x, this.y, this.x + boxWidth, this.y + boxHeight, 0xCC101010);
        Gui.drawRect(this.x, this.y, this.x + boxWidth, this.y + 2, 0xFF00A8FF);

        if (mainPreviewStack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(this.x + 6, this.y + 6, 0);
            GlStateManager.scale(1.5, 1.5, 1.5);
            renderItem(mainPreviewStack, 0, 0);
            GlStateManager.popMatrix();
        }

        int textX = this.x + 38;
        int textY = this.y + 6;

        fontRenderer.drawStringWithShadow(name, textX, textY, 0xFFFFFF);
        fontRenderer.drawStringWithShadow(modSource, textX, textY + 11, 0xFFFFFF);

        if (!extraInfo.isEmpty() && containerItems.isEmpty()) {
            fontRenderer.drawStringWithShadow(extraInfo, textX, textY + 22, 0xAAAAAA);
        }

        if (!containerItems.isEmpty()) {
            int gridStartX = this.x + 5;
            int gridStartY = this.y + 34;
            for (int i = 0; i < containerItems.size(); i++) {
                int col = i % 9;
                int row = i / 9;
                int drawX = gridStartX + (col * 18);
                int drawY = gridStartY + (row * 18);
                Gui.drawRect(drawX, drawY, drawX + 16, drawY + 16, 0x30FFFFFF);
                renderItem(containerItems.get(i), drawX, drawY);
            }
        }
    }

    private void renderItem(ItemStack stack, int x, int y) {
        if (stack == null) return;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);
        mc.getRenderItem().renderItemOverlays(mc.fontRendererObj, stack, x, y);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private String getModFromBlock(Block block) {
        try {
            String registry = Block.blockRegistry.getNameForObject(block).toString();
            if (registry.contains(":")) {
                String modid = registry.split(":")[0];
                if (modid.equalsIgnoreCase("minecraft")) return "Minecraft";
                return capitalize(modid);
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public int getWidth() { return 140; }
    @Override
    public int getHeight() { return 45; }
}