package com.vitorxp.SkyBlockModVX.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class GlowItemRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final RenderItem renderItem = mc.getRenderItem();
    private static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");


    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (!(obj instanceof EntityItem)) continue;

            EntityItem entity = (EntityItem) obj;
            ItemStack stack = entity.getEntityItem();

            if (!isSpecialItem(stack)) continue;

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - mc.getRenderManager().viewerPosX;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - mc.getRenderManager().viewerPosY;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - mc.getRenderManager().viewerPosZ;

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + 0.1, z);
            GlStateManager.rotate((entity.getAge() + event.partialTicks) * 2.0F, 0.0F, 1.0F, 0.0F);

            renderGlowEffect(stack);

            GlStateManager.popMatrix();
        }
    }

    private void renderGlowEffect(ItemStack stack) {
        GlStateManager.pushAttrib();

        GlStateManager.disableLighting();
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT_RES);

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.scale(0.125F, 0.125F, 0.125F);

        float time = (Minecraft.getSystemTime() % 3000L) / 3000.0F * 256.0F;
        GlStateManager.translate(time, 0.0F, 0.0F);
        GlStateManager.rotate( -50.0F, 0.0F, 0.0F, 1.0F);
        renderItem.renderItem(stack, renderItem.getItemModelMesher().getItemModel(stack));
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(0.125F, 0.125F, 0.125F);
        GlStateManager.translate(-time, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        renderItem.renderItem(stack, renderItem.getItemModelMesher().getItemModel(stack));
        GlStateManager.popMatrix();

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);

        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableLighting();

        GlStateManager.popAttrib();
    }

    private boolean isSpecialItem(ItemStack stack) {
        if (stack == null) return false;

        String name = stack.getDisplayName().toLowerCase();
        if (name.contains("summoning eye")) return true;

        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("display")) {
            NBTTagList loreList = stack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
            for (int i = 0; i < loreList.tagCount(); i++) {
                String loreLine = net.minecraft.util.StringUtils.stripControlCodes(loreList.getStringTagAt(i)).toLowerCase();
                if (loreLine.contains("lendário") || loreLine.contains("lendária") || loreLine.contains("lendario") || loreLine.contains("evento"))
                    return true;
            }
        }

        return false;
    }
}
