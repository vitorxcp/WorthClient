package com.vitorxp.WorthClient.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.vitorxp.WorthClient.utils.RankUtils.isStaffM;

public class TracerLineRenderer {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.RadarOverlay || mc.theWorld == null || mc.getRenderManager() == null || !isStaffM(mc.thePlayer)) {
            return;
        }

        if (mc.getNetHandler() == null) return;

        final Map<String, net.minecraft.client.network.NetworkPlayerInfo> playerInfoMap =
                mc.getNetHandler().getPlayerInfoMap().stream()
                        .collect(Collectors.toMap(
                                info -> info.getGameProfile().getName(),
                                info -> info,
                                (existingValue, newValue) -> newValue
                        ));

        List<EntityPlayer> nearbyPlayers = mc.theWorld.playerEntities.stream()
                .filter(player -> player != mc.thePlayer)
                .filter(player -> playerInfoMap.get(player.getName()) != null &&
                        playerInfoMap.get(player.getName()).getResponseTime() > 0)
                .collect(Collectors.toList());

        for (EntityPlayer player : nearbyPlayers) {
            float partialTicks = event.partialTicks;
            drawTracerLine(player, partialTicks);

            float distance = mc.thePlayer.getDistanceToEntity(player);

            if (distance > 10.0f) {
                drawNameTag(player, partialTicks, distance);
            }
        }
    }

    private void drawNameTag(EntityPlayer player, float partialTicks, float distance) {
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        String text = String.format("%s [§e%d m§f]", player.getName(), (int) distance);
        FontRenderer fontRenderer = mc.fontRendererObj;

        float scale = Math.max(1.5f, distance / 8.0f) * 0.025f;

        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY + player.height + 0.5F,
                z - mc.getRenderManager().viewerPosZ
        );
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);

        GlStateManager.disableDepth();

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        int textWidth = fontRenderer.getStringWidth(text);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(-textWidth / 2f - 2, -2, 0).color(0, 0, 0, 150).endVertex();
        worldrenderer.pos(-textWidth / 2f - 2, 9, 0).color(0, 0, 0, 150).endVertex();
        worldrenderer.pos(textWidth / 2f + 2, 9, 0).color(0, 0, 0, 150).endVertex();
        worldrenderer.pos(textWidth / 2f + 2, -2, 0).color(0, 0, 0, 150).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();

        fontRenderer.drawStringWithShadow(text, -textWidth / 2f, 0, 0xFFFFFF);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawTracerLine(EntityPlayer player, float partialTicks) {
        double viewerX = mc.getRenderManager().viewerPosX;
        double viewerY = mc.getRenderManager().viewerPosY;
        double viewerZ = mc.getRenderManager().viewerPosZ;

        double targetX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double targetY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double targetZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableTexture2D();
        GL11.glLineWidth(2.5F);

        float distance = mc.thePlayer.getDistanceToEntity(player);
        Color color = getColorFromDistance(distance);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        Vec3 startPoint = new Vec3(0, mc.thePlayer.getEyeHeight(), 0);
        double headY = targetY + player.getEyeHeight();
        double endX = targetX - viewerX;
        double endY = headY - viewerY;
        double endZ = targetZ - viewerZ;

        worldRenderer.pos(startPoint.xCoord, startPoint.yCoord, startPoint.zCoord)
                .color(color.getRed(), color.getGreen(), color.getBlue(), 180)
                .endVertex();
        worldRenderer.pos(endX, endY, endZ)
                .color(color.getRed(), color.getGreen(), color.getBlue(), 180)
                .endVertex();

        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private Color getColorFromDistance(float distance) {
        if (distance <= 15) return new Color(255, 0, 0);
        if (distance <= 40) return new Color(255, 165, 0);
        if (distance <= 80) return new Color(255, 255, 0);
        return new Color(0, 255, 0);
    }
}
