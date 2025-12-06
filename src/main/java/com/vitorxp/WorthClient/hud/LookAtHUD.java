package com.vitorxp.WorthClient.hud;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.ArrayList;
import java.util.List;

public class LookAtHUD extends HudElement {

    private final Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation GUI_ICONS = new ResourceLocation("textures/gui/icons.png");

    private String name = "";
    private String modSource = "";
    private String extraInfo = "";

    private ItemStack mainPreviewStack = null;
    private EntityLivingBase entityToRender = null;

    private final List<ItemStack> containerItems = new ArrayList<>();

    private float currentHealth = 0f;
    private float maxHealth = 0f;
    private boolean showHearts = false;

    public LookAtHUD() {
        super("LookAtHUD", 10, 70);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;

        resetData();

        MovingObjectPosition mop = mc.objectMouseOver;
        Entity targetEntity = null;

        if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            targetEntity = mop.entityHit;
        }

        if (targetEntity == null) {
            targetEntity = scanForItems();
        }

        if (targetEntity != null) {
            handleEntityHit(targetEntity);
        } else if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            handleBlockHit(mop);
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
        entityToRender = null;
        containerItems.clear();
        currentHealth = 0f;
        maxHealth = 0f;
        showHearts = false;
    }

    private EntityItem scanForItems() {
        Entity entity = mc.getRenderViewEntity();
        if (entity == null) return null;

        double distance = 4.0;
        Vec3 pos = entity.getPositionEyes(1.0F);
        Vec3 look = entity.getLook(1.0F);
        Vec3 reach = pos.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
        AxisAlignedBB box = entity.getEntityBoundingBox().addCoord(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance).expand(1.0, 1.0, 1.0);

        List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity, box);
        EntityItem closest = null;
        double minDst = distance * distance;

        for (Entity e : list) {
            if (e instanceof EntityItem) {
                float border = 0.5F;
                AxisAlignedBB hitBox = e.getEntityBoundingBox().expand(border, border, border);

                if (hitBox.isVecInside(pos)) {
                    if (0.0 < minDst) {
                        closest = (EntityItem) e;
                        minDst = 0.0;
                    }
                } else {
                    MovingObjectPosition intercept = hitBox.calculateIntercept(pos, reach);
                    if (intercept != null) {
                        double dst = pos.squareDistanceTo(intercept.hitVec);
                        if (dst < minDst) {
                            closest = (EntityItem) e;
                            minDst = dst;
                        }
                    }
                }
            }
        }
        return closest;
    }

    private void handleBlockHit(MovingObjectPosition mop) {
        BlockPos pos = mop.getBlockPos();
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block block = state.getBlock();

        if (block != null && block != net.minecraft.init.Blocks.air) {
            mainPreviewStack = block.getPickBlock(mop, mc.theWorld, pos, mc.thePlayer);

            if (mainPreviewStack == null) {
                Item itemFromBlock = Item.getItemFromBlock(block);
                if (itemFromBlock != null) {
                    mainPreviewStack = new ItemStack(block);
                }
            }

            if (mainPreviewStack == null || mainPreviewStack.getItem() == null) {
                return;
            }

            try {
                name = mainPreviewStack.getDisplayName();
            } catch (Exception e) {
                name = "Unknown Block";
            }

            modSource = EnumChatFormatting.BLUE + "" + EnumChatFormatting.ITALIC + getModFromObject(block);

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

    private void handleEntityHit(Entity e) {
        if (e == null) return;

        if (e instanceof EntityItem) {
            EntityItem entityItem = (EntityItem) e;
            ItemStack itemStack = entityItem.getEntityItem();

            if (itemStack != null && itemStack.getItem() != null) {
                mainPreviewStack = itemStack.copy();
                name = itemStack.getDisplayName();

                if (itemStack.stackSize > 1) {
                    extraInfo = EnumChatFormatting.WHITE + "Qtd: " + EnumChatFormatting.YELLOW + "x" + itemStack.stackSize;
                }

                modSource = EnumChatFormatting.BLUE + "" + EnumChatFormatting.ITALIC + getModFromObject(itemStack.getItem());
            }
            return;
        }

        if (e instanceof EntityArmorStand) {
            EntityArmorStand stand = (EntityArmorStand) e;

            ItemStack headItem = stand.getEquipmentInSlot(4);
            ItemStack handItem = stand.getEquipmentInSlot(0);

            ItemStack displayItem = null;
            if (headItem != null && headItem.getItem() != null) {
                displayItem = headItem.copy();
            }

            if (displayItem == null) {
                displayItem = searchForNearbyHead(stand);
            }

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
            } else if (foundName.contains("Pet") || (stand.isSmall())) {
                if (foundName.contains("Pet")) isPet = true;
                else isMinion = true;

                if (foundName.isEmpty() || foundName.equals("Armor Stand")) {
                    foundName = EnumChatFormatting.YELLOW + "Minion";
                }
            }

            if (isMinion) {
                name = foundName;
                modSource = EnumChatFormatting.BLUE + "SkyBlock Minion";
                if (headItem != null && headItem.getItem() != null) {
                    mainPreviewStack = headItem.copy();
                } else {
                    mainPreviewStack = (displayItem != null) ? displayItem : new ItemStack(net.minecraft.init.Items.armor_stand);
                }

            } else if (isPet) {
                name = foundName;
                modSource = EnumChatFormatting.BLUE + "SkyBlock Pet";

                ItemStack foundPetItem = searchForPetItem(stand);

                if (foundPetItem != null) {
                    mainPreviewStack = foundPetItem.copy();
                } else if (handItem != null && handItem.getItem() != null) {
                    mainPreviewStack = handItem.copy();
                } else {
                    mainPreviewStack = (displayItem != null) ? displayItem : new ItemStack(net.minecraft.init.Items.skull, 1, 3);
                }

            } else {
                name = e.hasCustomName() ? e.getCustomNameTag() : EnumChatFormatting.WHITE + "Armor Stand";
                modSource = EnumChatFormatting.BLUE + "Minecraft";
                mainPreviewStack = (displayItem != null) ? displayItem : new ItemStack(net.minecraft.init.Items.armor_stand);
            }
            return;
        }

        if (e instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) e;

            entityToRender = living;
            currentHealth = living.getHealth();
            maxHealth = living.getMaxHealth();
            showHearts = maxHealth > 0;

            if (e instanceof EntityPlayer) {
                name = EnumChatFormatting.GREEN + e.getName();
                modSource = EnumChatFormatting.YELLOW + "Jogador";
            } else {
                name = e.getName();
                if (e.hasCustomName()) name = e.getCustomNameTag();

                String entityName = EntityList.getEntityString(e);
                if (entityName == null) entityName = "NPC";
                modSource = EnumChatFormatting.GRAY + entityName;
            }
        }
    }

    private ItemStack searchForPetItem(EntityArmorStand source) {
        AxisAlignedBB searchBox = source.getEntityBoundingBox().expand(2.0, 2.0, 2.0);
        List<EntityArmorStand> nearby = mc.theWorld.getEntitiesWithinAABB(EntityArmorStand.class, searchBox);

        for (EntityArmorStand neighbor : nearby) {
            if (neighbor == source) continue;
            ItemStack rightHand = neighbor.getEquipmentInSlot(0);
            if (rightHand != null && rightHand.getItem() != null && rightHand.getItem() != net.minecraft.init.Blocks.air.getItem(mc.theWorld, null)) {
                return rightHand.copy();
            }
        }
        return null;
    }

    private ItemStack searchForNearbyHead(EntityArmorStand source) {
        AxisAlignedBB searchBox = source.getEntityBoundingBox().expand(1.0, 1.0, 1.0);
        List<EntityArmorStand> nearby = mc.theWorld.getEntitiesWithinAABB(EntityArmorStand.class, searchBox);

        for (EntityArmorStand neighbor : nearby) {
            if (neighbor == source) continue;
            ItemStack head = neighbor.getEquipmentInSlot(4);
            if (head != null && head.getItem() != null && head.getItem() != net.minecraft.init.Blocks.air.getItem(mc.theWorld, null)) {
                return head.copy();
            }
        }
        return null;
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
        int nameW = fontRenderer.getStringWidth(name);
        int modW = fontRenderer.getStringWidth(modSource);
        int infoW = extraInfo.isEmpty() ? 0 : fontRenderer.getStringWidth(extraInfo);

        int heartsWidth = 0;
        if (showHearts) {
            int heartsCount = (int) Math.ceil(maxHealth / 2.0f);
            heartsWidth = heartsCount * 8;
        }

        int maxTextWidth = Math.max(nameW, Math.max(modW, infoW));
        maxTextWidth = Math.max(maxTextWidth, heartsWidth);

        int paddingSide = 42;
        int boxWidth = Math.max(140, maxTextWidth + paddingSide);

        if (!containerItems.isEmpty()) {
            int itemsPerRow = 9;
            int inventoryNeededWidth = (itemsPerRow * 18) + 10;
            boxWidth = Math.max(boxWidth, inventoryNeededWidth);
        }

        int headerHeight = 34;
        if (!extraInfo.isEmpty() && containerItems.isEmpty()) {
            headerHeight = 45;
        }

        if (showHearts) {
            headerHeight += 12;
        }

        int inventoryHeight = 0;
        if (!containerItems.isEmpty()) {
            int rows = (int) Math.ceil((double) containerItems.size() / 9.0);
            inventoryHeight = (rows * 18) + 6;
        }

        int boxHeight = headerHeight + inventoryHeight;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        this.x = (screenWidth / 2) - (boxWidth / 2);

        Gui.drawRect(this.x, this.y, this.x + boxWidth, this.y + boxHeight, 0xCC101010);
        Gui.drawRect(this.x, this.y, this.x + boxWidth, this.y + 2, 0xFF00A8FF);

        int textX = this.x + 38;
        int textY = this.y + 6;

        if (entityToRender != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            try {
                drawEntityOnScreen(this.x + 19, this.y + 35, 14, entityToRender);
            } catch (Exception e) {
                entityToRender = null;
            }
        }

        if (entityToRender == null && mainPreviewStack != null) {
            GlStateManager.pushMatrix();
            float iconY = this.y + (headerHeight - (16 * 1.5f)) / 2;
            if (iconY < this.y + 4) iconY = this.y + 4;
            if (showHearts) iconY -= 6;

            GlStateManager.translate(this.x + 6, iconY, 0);
            GlStateManager.scale(1.5, 1.5, 1.5);
            renderItem(mainPreviewStack, 0, 0);
            GlStateManager.popMatrix();
        }

        fontRenderer.drawStringWithShadow(name, textX, textY, 0xFFFFFF);

        int currentY = textY + 11;

        if (showHearts) {
            drawHearts(textX, currentY);
            currentY += 11;
        }

        fontRenderer.drawStringWithShadow(modSource, textX, currentY, 0xFFFFFF);

        if (!extraInfo.isEmpty() && containerItems.isEmpty()) {
            fontRenderer.drawStringWithShadow(extraInfo, textX, currentY + 11, 0xAAAAAA);
        }

        if (!containerItems.isEmpty()) {
            int gridStartX = this.x + (boxWidth - (9 * 18)) / 2;
            if (gridStartX < this.x + 5) gridStartX = this.x + 5;
            int gridStartY = this.y + headerHeight + 2;

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

    private void drawEntityOnScreen(int posX, int posY, int scale, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);

        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;

        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-((float)Math.atan(10.0F / 40.0F)) * 20.0F, 1.0F, 0.0F, 0.0F);

        ent.renderYawOffset = (float)Math.atan(0.0F) * 20.0F;
        ent.rotationYaw = (float)Math.atan(0.0F) * 40.0F;
        ent.rotationPitch = -((float)Math.atan(10.0F / 40.0F)) * 20.0F;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationYawHead = ent.rotationYaw;

        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        mc.getRenderManager().playerViewY = 180.0F;
        mc.getRenderManager().renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);

        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.disableDepth();
    }

    private void drawHearts(int x, int y) {
        mc.getTextureManager().bindTexture(GUI_ICONS);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int heartsToDraw = (int) Math.ceil(maxHealth / 2.0f);
        if (heartsToDraw > 20) heartsToDraw = 20; // Limite visual

        for (int i = 0; i < heartsToDraw; i++) {
            int drawX = x + (i * 8);

            mc.ingameGUI.drawTexturedModalRect(drawX, y, 16, 0, 9, 9); // Vazio

            if (i * 2 + 1 < (int)currentHealth) {
                mc.ingameGUI.drawTexturedModalRect(drawX, y, 52, 0, 9, 9); // Cheio
            } else if (i * 2 + 1 == (int)currentHealth) {
                mc.ingameGUI.drawTexturedModalRect(drawX, y, 61, 0, 9, 9); // Metade
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
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

    private String getModFromObject(Object object) {
        try {
            ResourceLocation reg = null;
            if (object instanceof Block) {
                reg = Block.blockRegistry.getNameForObject((Block) object);
            } else if (object instanceof Item) {
                reg = Item.itemRegistry.getNameForObject((Item) object);
            }

            if (reg != null) {
                String modid = reg.getResourceDomain();
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