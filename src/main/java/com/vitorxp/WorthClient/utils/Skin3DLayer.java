package com.vitorxp.WorthClient.utils;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;

public class Skin3DLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer renderPlayer;

    public Skin3DLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.isInvisible()) return;

        this.renderPlayer.bindTexture(player.getLocationSkin());

        ModelPlayer model = this.renderPlayer.getMainModel();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.disableCull();

        GlStateManager.pushMatrix();

        float scale3D = 1.051F;

        if (player.isWearing(EnumPlayerModelParts.HAT)) {
            GlStateManager.pushMatrix();
            if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
            model.bipedHead.postRender(0.0625F);
            GlStateManager.scale(scale3D, scale3D, scale3D);
            GlStateManager.translate(-0.0F, -0.0F, 0.0F);
            model.bipedHeadwear.render(0.0625F);
            GlStateManager.popMatrix();
        }

        if (player.isWearing(EnumPlayerModelParts.JACKET)) {
            GlStateManager.pushMatrix();
            if (player.isSneaking()) GlStateManager.translate(0.0F, 0.2F, 0.0F);
            model.bipedBody.postRender(0.0625F);
            GlStateManager.scale(scale3D, scale3D, scale3D);
            model.bipedBody.render(0.0625F);
            GlStateManager.popMatrix();
        }

        renderPart(player, model.bipedLeftArm, EnumPlayerModelParts.LEFT_SLEEVE, scale3D);
        renderPart(player, model.bipedRightArm, EnumPlayerModelParts.RIGHT_SLEEVE, scale3D);
        renderPart(player, model.bipedLeftLeg, EnumPlayerModelParts.LEFT_PANTS_LEG, scale3D);
        renderPart(player, model.bipedRightLeg, EnumPlayerModelParts.RIGHT_PANTS_LEG, scale3D);

        GlStateManager.popMatrix();

        GlStateManager.enableCull();
        GlStateManager.disableBlend();
    }

    private void renderPart(AbstractClientPlayer player, net.minecraft.client.model.ModelRenderer modelPart, EnumPlayerModelParts part, float scale) {
        if (player.isWearing(part)) {
            GlStateManager.pushMatrix();
            if (player.isSneaking()) {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            modelPart.postRender(0.0625F);
            GlStateManager.scale(scale, scale, scale);
            modelPart.render(0.0625F);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}