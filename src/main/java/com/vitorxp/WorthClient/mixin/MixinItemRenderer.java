package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.config.AnimationsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow @Final private Minecraft mc;
    @Shadow private float prevEquippedProgress;
    @Shadow private float equippedProgress;
    @Shadow private ItemStack itemToRender;

    @Shadow protected abstract void transformFirstPersonItem(float equipProgress, float swingProgress);
    @Shadow protected abstract void doBlockTransformations();
    @Shadow protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer player);
    @Shadow protected abstract void doItemUsedTransformations(float swingProgress);
    @Shadow protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Inject(method = "renderItemInFirstPerson", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;pushMatrix()V", shift = At.Shift.AFTER))
    private void modifyItemPosition(float partialTicks, CallbackInfo ci) {
        if (AnimationsConfig.enabled) {
            GlStateManager.translate(AnimationsConfig.itemPosX, AnimationsConfig.itemPosY, AnimationsConfig.itemPosZ);
            if (AnimationsConfig.itemScale != 1.0f) {
                GlStateManager.scale(AnimationsConfig.itemScale, AnimationsConfig.itemScale, AnimationsConfig.itemScale);
            }
        }
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onRenderItemInFirstPerson(float partialTicks, CallbackInfo ci) {
        if (!AnimationsConfig.enabled) return;

        float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);

        EntityPlayerSP player = this.mc.thePlayer;
        float swingProgress = player.getSwingProgress(partialTicks);
        float f2 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float f3 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;

        ItemStack itemstack = this.itemToRender;

        if (itemstack == null) return;

        if (AnimationsConfig.blockHit17 && player.getItemInUseCount() > 0 && itemstack.getItemUseAction() == EnumAction.BLOCK) {
            ci.cancel();
            doRenderBlockHit(player, f, swingProgress, f2, f3, itemstack);
            return;
        }

        if (AnimationsConfig.oldBow && player.getItemInUseCount() > 0 && itemstack.getItemUseAction() == EnumAction.BOW) {
            ci.cancel();
            doRenderBow(player, f, partialTicks, itemstack);
            return;
        }
    }

    private void doRenderBlockHit(EntityPlayerSP player, float equipProgress, float swingProgress, float pitch, float yaw, ItemStack itemstack) {
        GlStateManager.pushMatrix();

        GlStateManager.translate(AnimationsConfig.itemPosX, AnimationsConfig.itemPosY, AnimationsConfig.itemPosZ);
        if (AnimationsConfig.itemScale != 1.0f) GlStateManager.scale(AnimationsConfig.itemScale, AnimationsConfig.itemScale, AnimationsConfig.itemScale);

        int i = this.mc.theWorld.getCombinedLight(new net.minecraft.util.BlockPos(player.posX, player.posY + (double)player.getEyeHeight(), player.posZ), 0);
        float f4 = (float)(i & 65535);
        float f5 = (float)(i >> 16);
        net.minecraft.client.renderer.OpenGlHelper.setLightmapTextureCoords(net.minecraft.client.renderer.OpenGlHelper.lightmapTexUnit, f4, f5);

        GlStateManager.rotate(pitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(yaw, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        this.transformFirstPersonItem(equipProgress, swingProgress);
        this.doBlockTransformations();
        GlStateManager.scale(0.83f, 0.83f, 0.83f);
        this.mc.getItemRenderer().renderItem(player, itemstack, ItemCameraTransforms.TransformType.FIRST_PERSON);
        GlStateManager.popMatrix();

        RenderHelper.disableStandardItemLighting();
    }

    private void doRenderBow(EntityPlayerSP player, float equipProgress, float partialTicks, ItemStack itemstack) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(AnimationsConfig.itemPosX, AnimationsConfig.itemPosY, AnimationsConfig.itemPosZ);
        if (AnimationsConfig.itemScale != 1.0f) GlStateManager.scale(AnimationsConfig.itemScale, AnimationsConfig.itemScale, AnimationsConfig.itemScale);

        this.transformFirstPersonItem(equipProgress, 0.0F);
        this.doBowTransformations(partialTicks, player);
        this.mc.getItemRenderer().renderItem(player, itemstack, ItemCameraTransforms.TransformType.FIRST_PERSON);
        GlStateManager.popMatrix();
    }
}