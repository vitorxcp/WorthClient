package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.socket.ClientSocket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity<T extends EntityLivingBase> {

    private static final ResourceLocation WORTH_ICON = new ResourceLocation("worthclient", "icons/icon.png");
    private static final Pattern CLEANER = Pattern.compile("[^a-zA-Z0-9_]");

    @Shadow
    protected abstract boolean canRenderName(T entity);

    @Inject(method = {"renderName", "func_77033_b"}, at = @At("HEAD"), cancellable = true)
    public void onRenderName(T entity, double x, double y, double z, CallbackInfo ci) {
        if (!this.canRenderName(entity)) {
            return;
        }

        boolean isValidEntity = (entity instanceof EntityPlayer) || (entity instanceof EntityArmorStand);

        if (isValidEntity) {
            String originalText = entity.getDisplayName().getFormattedText();
            String cleanText = cleanString(originalText);

            if (checkMatch(cleanText)) {
                renderCustomLabel(entity, x, y, z, originalText);
                ci.cancel();
            }
        }
    }

    private void renderCustomLabel(EntityLivingBase entityIn, double x, double y, double z, String str) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fontrenderer = mc.fontRendererObj;
        float f = 1.6F;
        float f1 = 0.016666668F * f;

        GlStateManager.pushMatrix();
        GlStateManager.translate((float)x + 0.0F, (float)y + entityIn.height + 0.5F, (float)z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);

        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);

        GlStateManager.scale(-f1, -f1, f1);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        int strWidth = fontrenderer.getStringWidth(str);
        int totalWidth = strWidth + 12;
        int halfWidth = totalWidth / 2;

        GlStateManager.disableTexture2D();

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos((double)(-halfWidth - 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(-halfWidth - 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(halfWidth + 1), 8.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        worldrenderer.pos((double)(halfWidth + 1), -1.0D, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(WORTH_ICON);
        drawIcon(-halfWidth, -1, 8, 8);

        int textX = -halfWidth + 11;
        int color = 553648127;

        fontrenderer.drawString(str, textX, 0, color);

        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        fontrenderer.drawString(str, textX, 0, -1);

        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private void drawIcon(int x, int y, int width, int height) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
    }

    private String cleanString(String input) {
        if (input == null) return "";
        String noColors = EnumChatFormatting.getTextWithoutFormattingCodes(input);
        return CLEANER.matcher(noColors).replaceAll("").toLowerCase();
    }

    private boolean checkMatch(String cleanDisplay) {
        if (cleanDisplay.isEmpty()) return false;

        try {
            String myNick = Minecraft.getMinecraft().getSession().getUsername();
            if (myNick != null && cleanDisplay.contains(cleanString(myNick))) {
                return true;
            }
        } catch (Exception ignored) {}

        if (ClientSocket.usersUsingClient != null) {
            for (String socketUser : ClientSocket.usersUsingClient) {
                if (socketUser != null && cleanDisplay.contains(socketUser.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }
}