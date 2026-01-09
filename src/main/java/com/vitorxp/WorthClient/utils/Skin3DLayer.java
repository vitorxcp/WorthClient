package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EnumPlayerModelParts;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

public class Skin3DLayer implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer renderPlayer;

    private static final Map<String, Integer> displayLists = new HashMap<>();

    public Skin3DLayer(RenderPlayer renderPlayer) {
        this.renderPlayer = renderPlayer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (!WorthClient.skin3D) return;
        if (player.isInvisible() || !player.hasSkin()) return;

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        this.renderPlayer.bindTexture(player.getLocationSkin());
        ModelPlayer model = this.renderPlayer.getMainModel();
        boolean isSlim = player.getSkinType().equals("slim");

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableCull();

        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
        }

        if (player.isWearing(EnumPlayerModelParts.HAT)) {
            renderPixelatedPart(player, 8, 8, 8, 32, 0, -4.0F, -8.0F, -4.0F, () -> model.bipedHead.postRender(0.0625F));
        }
        if (player.isWearing(EnumPlayerModelParts.JACKET)) {
            renderPixelatedPart(player, 8, 12, 4, 16, 32, -4.0F, 0.0F, -2.0F, () -> model.bipedBody.postRender(0.0625F));
        }
        if (player.isWearing(EnumPlayerModelParts.LEFT_SLEEVE)) {
            int w = isSlim ? 3 : 4;
            renderPixelatedPart(player, w, 12, 4, 48, 48, -1.0F, -2.0F, -2.0F, () -> model.bipedLeftArm.postRender(0.0625F));
        }
        if (player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE)) {
            int w = isSlim ? 3 : 4;
            float offX = isSlim ? -2.0F : -3.0F;
            renderPixelatedPart(player, w, 12, 4, 40, 32, offX, -2.0F, -2.0F, () -> model.bipedRightArm.postRender(0.0625F));
        }
        if (player.isWearing(EnumPlayerModelParts.LEFT_PANTS_LEG)) {
            renderPixelatedPart(player, 4, 12, 4, 0, 48, -2.0F, 0.0F, -2.0F, () -> model.bipedLeftLeg.postRender(0.0625F));
        }
        if (player.isWearing(EnumPlayerModelParts.RIGHT_PANTS_LEG)) {
            renderPixelatedPart(player, 4, 12, 4, 0, 32, -2.0F, 0.0F, -2.0F, () -> model.bipedRightLeg.postRender(0.0625F));
        }

        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    public static void renderFirstPersonArm(RenderPlayer renderPlayer, AbstractClientPlayer player) {
        if (!player.hasSkin() || player.isInvisible()) return;
        if (!player.isWearing(EnumPlayerModelParts.RIGHT_SLEEVE)) return;

        renderPlayer.bindTexture(player.getLocationSkin());
        ModelPlayer model = renderPlayer.getMainModel();
        boolean isSlim = player.getSkinType().equals("slim");

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        GlStateManager.disableCull();

        model.bipedRightArm.postRender(0.0625F);

        int w = isSlim ? 3 : 4;
        float offX = isSlim ? -2.0F : -3.0F;

        renderRawVoxels(player, "right_arm_fp_" + (isSlim ? "s" : "n"), w, 12, 4, 40, 32, offX, -2.0F, -2.0F);

        GlStateManager.enableCull();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
    }

    private void renderPixelatedPart(AbstractClientPlayer player, int w, int h, int d, int u, int v, float offX, float offY, float offZ, Runnable transform) {
        GlStateManager.pushMatrix();
        transform.run();
        String id = (w == 8 && h == 8) ? "head" : (h == 12 && d == 4) ? (offY == 0 ? "body" : "limb") : "other";
        renderRawVoxels(player, id + u + v, w, h, d, u, v, offX, offY, offZ);
        GlStateManager.popMatrix();
    }

    private static void renderRawVoxels(AbstractClientPlayer player, String partId, int w, int h, int d, int u, int v, float offX, float offY, float offZ) {
        float pixel = 0.0625F;
        GlStateManager.translate(offX * pixel, offY * pixel, offZ * pixel);

        String key = player.getUniqueID() + ":" + partId + ":" + WorthClient.pixelsThickness;

        if (displayLists.containsKey(key)) {
            GlStateManager.callList(displayLists.get(key));
        } else {
            int listId = GL11.glGenLists(1);
            GL11.glNewList(listId, GL11.GL_COMPILE);
            buildVoxels(w, h, d, u, v);
            GL11.glEndList();
            displayLists.put(key, listId);
            GlStateManager.callList(listId);
        }
    }

    private static void buildVoxels(int width, int height, int depth, int uStart, int vStart) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        float pixel = 0.0625F;

        float extrusion = pixel * WorthClient.pixelsThickness * 0.5F;

        wr.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);

        for (int px = 0; px < width; px++) {
            for (int py = 0; py < height; py++) {
                drawExtrudedPixel(wr, px * pixel, py * pixel, 0.0F, pixel, extrusion, uStart + depth + px, vStart + depth + py, 0, 0, -1);
            }
        }
        for (int px = 0; px < width; px++) {
            for (int py = 0; py < height; py++) {
                drawExtrudedPixel(wr, (width - 1 - px) * pixel, py * pixel, depth * pixel, pixel, extrusion, uStart + depth + width + depth + px, vStart + depth + py, 0, 0, 1);
            }
        }
        for (int pz = 0; pz < depth; pz++) {
            for (int py = 0; py < height; py++) {
                drawExtrudedPixel(wr, 0.0F, py * pixel, (depth - 1 - pz) * pixel, pixel, extrusion, uStart + pz, vStart + depth + py, -1, 0, 0);
            }
        }
        for (int pz = 0; pz < depth; pz++) {
            for (int py = 0; py < height; py++) {
                drawExtrudedPixel(wr, width * pixel, py * pixel, pz * pixel, pixel, extrusion, uStart + depth + width + pz, vStart + depth + py, 1, 0, 0);
            }
        }
        for (int px = 0; px < width; px++) {
            for (int pz = 0; pz < depth; pz++) {
                drawExtrudedPixel(wr, px * pixel, 0.0F, pz * pixel, pixel, extrusion, uStart + depth + px, vStart + pz, 0, -1, 0);
            }
        }
        for (int px = 0; px < width; px++) {
            for (int pz = 0; pz < depth; pz++) {
                drawExtrudedPixel(wr, px * pixel, height * pixel, pz * pixel, pixel, extrusion, uStart + depth + width + px, vStart + pz, 0, 1, 0);
            }
        }
        tessellator.draw();
    }

    private static void drawExtrudedPixel(WorldRenderer wr, float x, float y, float z, float size, float thick, int u, int v, float nx, float ny, float nz) {
        float texSize = 64.0F;
        float minU = u / texSize;
        float maxU = (u + 1) / texSize;
        float minV = v / texSize;
        float maxV = (v + 1) / texSize;
        float xOut = x + (nx * thick);
        float yOut = y + (ny * thick);
        float zOut = z + (nz * thick);

        float[][] c = new float[4][3];
        if (nx != 0) {
            c[0] = new float[]{0, size, 0}; c[1] = new float[]{0, size, size}; c[2] = new float[]{0, 0, size}; c[3] = new float[]{0, 0, 0};
        } else if (ny != 0) {
            c[0] = new float[]{0, 0, 0}; c[1] = new float[]{0, 0, size}; c[2] = new float[]{size, 0, size}; c[3] = new float[]{size, 0, 0};
        } else {
            c[0] = new float[]{0, size, 0}; c[1] = new float[]{size, size, 0}; c[2] = new float[]{size, 0, 0}; c[3] = new float[]{0, 0, 0};
        }

        wr.pos(xOut + c[0][0], yOut + c[0][1], zOut + c[0][2]).tex(minU, maxV).normal(nx, ny, nz).endVertex();
        wr.pos(xOut + c[1][0], yOut + c[1][1], zOut + c[1][2]).tex(maxU, maxV).normal(nx, ny, nz).endVertex();
        wr.pos(xOut + c[2][0], yOut + c[2][1], zOut + c[2][2]).tex(maxU, minV).normal(nx, ny, nz).endVertex();
        wr.pos(xOut + c[3][0], yOut + c[3][1], zOut + c[3][2]).tex(minU, minV).normal(nx, ny, nz).endVertex();

        float midU = (minU + maxU) / 2.0F;
        float midV = (minV + maxV) / 2.0F;
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            wr.pos(x + c[i][0], y + c[i][1], z + c[i][2]).tex(midU, midV).normal(0, 0, 0).endVertex();
            wr.pos(x + c[next][0], y + c[next][1], z + c[next][2]).tex(midU, midV).normal(0, 0, 0).endVertex();
            wr.pos(xOut + c[next][0], yOut + c[next][1], zOut + c[next][2]).tex(midU, midV).normal(0, 0, 0).endVertex();
            wr.pos(xOut + c[i][0], yOut + c[i][1], zOut + c[i][2]).tex(midU, midV).normal(0, 0, 0).endVertex();
        }
    }

    @Override
    public boolean shouldCombineTextures() { return false; }
}