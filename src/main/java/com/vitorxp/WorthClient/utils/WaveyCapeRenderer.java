package com.vitorxp.WorthClient.utils;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class WaveyCapeRenderer {
    public void render(AbstractClientPlayer player, float partialTicks, AnimatedCape animCape) {
        if (!(player instanceof CapeSimulationHolder)) return;
        StickSimulation simulation = ((CapeSimulationHolder) player).getSimulation();
        if (simulation.points.isEmpty()) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0.0D, 0.0D, 0.125D);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        if (player.isSneaking()) {
            GlStateManager.translate(0.0F, 0.2F, 0.0F);
            GlStateManager.rotate(25.0F, 1.0F, 0.0F, 0.0F);
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        for (int part = 0; part < 16; part++) {
            GlStateManager.pushMatrix();
            StickSimulation.Point point = simulation.points.get(part);
            StickSimulation.Point nextPoint = (part < 15) ? simulation.points.get(part + 1) : null;
            StickSimulation.Point point0 = simulation.points.get(0);
            float smoothX = point.getLerpX(partialTicks) - point0.getLerpX(partialTicks);
            float smoothY = point.getLerpY(partialTicks) - point0.getLerpY(partialTicks);
            float smoothZ = point.getLerpZ(partialTicks) - point0.getLerpZ(partialTicks);
            GlStateManager.translate(smoothX, smoothY, smoothZ);
            if (nextPoint != null) {
                float deltaY = nextPoint.getLerpY(partialTicks) - point.getLerpY(partialTicks);
                float deltaZ = nextPoint.getLerpZ(partialTicks) - point.getLerpZ(partialTicks);
                float deltaX = nextPoint.getLerpX(partialTicks) - point.getLerpX(partialTicks);
                double angleX = Math.atan2(deltaZ, deltaY);
                float rotX = (float) Math.toDegrees(angleX);
                GlStateManager.rotate(rotX + 90.0F, 1.0F, 0.0F, 0.0F);
                double angleZ = Math.atan2(deltaX, deltaY);
                float rotZ = (float) Math.toDegrees(angleZ);
                GlStateManager.rotate(-rotZ, 0.0F, 0.0F, 1.0F);
            }
            float vMin, vMax;
            if (animCape != null && animCape.getTotalFrames() > 1) {
                float frameHeight = 1.0F / (float) animCape.getTotalFrames();
                float frameOffset = (float) animCape.getCurrentFrame() * frameHeight;
                vMin = frameOffset + (part * (frameHeight / 16.0F));
                vMax = frameOffset + ((part + 1) * (frameHeight / 16.0F));
            } else {
                vMin = (float) part / 16.0F;
                vMax = (float) (part + 1) / 16.0F;
            }
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
            float w = 0.6F;
            float h = 1.0F / 16.0F;
            float d = 0.04F;
            float x1 = -w / 2.0F;
            float x2 = w / 2.0F;
            float y1 = 0;
            float y2 = -h;
            worldrenderer.pos(x1, y1, d).tex(0, vMin).normal(0, 0, 1).endVertex();
            worldrenderer.pos(x1, y2, d).tex(0, vMax).normal(0, 0, 1).endVertex();
            worldrenderer.pos(x2, y2, d).tex(1, vMax).normal(0, 0, 1).endVertex();
            worldrenderer.pos(x2, y1, d).tex(1, vMin).normal(0, 0, 1).endVertex();
            worldrenderer.pos(x1, y2, 0).tex(0, vMax).normal(0, 0, -1).endVertex();
            worldrenderer.pos(x1, y1, 0).tex(0, vMin).normal(0, 0, -1).endVertex();
            worldrenderer.pos(x2, y1, 0).tex(1, vMin).normal(0, 0, -1).endVertex();
            worldrenderer.pos(x2, y2, 0).tex(1, vMax).normal(0, 0, -1).endVertex();
            worldrenderer.pos(x1, y1, 0).tex(0, vMin).normal(-1, 0, 0).endVertex();
            worldrenderer.pos(x1, y2, 0).tex(0, vMax).normal(-1, 0, 0).endVertex();
            worldrenderer.pos(x1, y2, d).tex(0.05, vMax).normal(-1, 0, 0).endVertex();
            worldrenderer.pos(x1, y1, d).tex(0.05, vMin).normal(-1, 0, 0).endVertex();
            worldrenderer.pos(x2, y1, d).tex(0.05, vMin).normal(1, 0, 0).endVertex();
            worldrenderer.pos(x2, y2, d).tex(0.05, vMax).normal(1, 0, 0).endVertex();
            worldrenderer.pos(x2, y2, 0).tex(0, vMax).normal(1, 0, 0).endVertex();
            worldrenderer.pos(x2, y1, 0).tex(0, vMin).normal(1, 0, 0).endVertex();
            worldrenderer.pos(x1, y1, 0).tex(0.05, vMin).normal(0, 1, 0).endVertex();
            worldrenderer.pos(x1, y1, d).tex(0.05, vMin).normal(0, 1, 0).endVertex();
            worldrenderer.pos(x2, y1, d).tex(0.05, vMin).normal(0, 1, 0).endVertex();
            worldrenderer.pos(x2, y1, 0).tex(0.05, vMin).normal(0, 1, 0).endVertex();
            worldrenderer.pos(x1, y2, d).tex(0.05, vMax).normal(0, -1, 0).endVertex();
            worldrenderer.pos(x1, y2, 0).tex(0.05, vMax).normal(0, -1, 0).endVertex();
            worldrenderer.pos(x2, y2, 0).tex(0.05, vMax).normal(0, -1, 0).endVertex();
            worldrenderer.pos(x2, y2, d).tex(0.05, vMax).normal(0, -1, 0).endVertex();
            tessellator.draw();
            GlStateManager.popMatrix();
        }
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
}