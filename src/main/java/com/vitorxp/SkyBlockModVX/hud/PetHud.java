package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;

import static com.vitorxp.SkyBlockModVX.manager.ActivationManager.isActivated;

public class PetHud {
    public static String messageDisplayHUDPet = "Pet Ativo: §6" + SkyBlockMod.currentPetName + " §7(Lvl " + SkyBlockMod.currentPetlevel + ")";

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null || mc.thePlayer == null) return;

            boolean petEncontrado = false;

            for (Entity entity : new ArrayList<>(mc.theWorld.loadedEntityList)) {
                if (!(entity instanceof EntityArmorStand)) continue;

                String name = entity.getCustomNameTag();
                if (name == null || name.isEmpty()) continue;

                String cleanName = net.minecraft.util.StringUtils.stripControlCodes(name);

                if (SkyBlockMod.viewsPetAll && cleanName.startsWith("[Lvl") && cleanName.contains("'s") && isActivated) {
                    mc.theWorld.removeEntity(entity);

                    for (Entity nearby : new ArrayList<>(mc.theWorld.loadedEntityList)) {
                        if (nearby instanceof EntityArmorStand && nearby != entity) {
                            double distance = entity.getDistanceToEntity(nearby);
                            if (distance < 1.5) {
                                mc.theWorld.removeEntity(nearby);
                            }
                        }
                    }

                    if (cleanName.contains(mc.thePlayer.getName() + "'s")) {
                        parseAndDisplayPet(name);
                        petEncontrado = true;
                    }

                    continue;
                }

                if (cleanName.startsWith("[Lvl") && cleanName.contains(mc.thePlayer.getName() + "'s")) {

                    if (SkyBlockMod.petDisplayViewOff && isActivated) {
                        parseAndDisplayPet(name);
                        mc.theWorld.removeEntity(entity);

                        for (Entity nearby : new ArrayList<>(mc.theWorld.loadedEntityList)) {
                            if (nearby instanceof EntityArmorStand && nearby != entity) {
                                double distance = entity.getDistanceToEntity(nearby);
                                if (distance < 1.5) {
                                    mc.theWorld.removeEntity(nearby);
                                }
                            }
                        }

                        parseAndDisplayPet(name);
                        return;
                    } else {
                        parseAndDisplayPet(name);
                        petEncontrado = true;
                        break;
                    }
                }
            }

            if (!petEncontrado && !(SkyBlockMod.petDisplayViewOff && isActivated)) {
                SkyBlockMod.currentPetName = "§cSem Pet Equipado";
                SkyBlockMod.currentPetlevel = "0";
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Text event) {
        if(SkyBlockMod.petOverlay && isActivated) {
            Minecraft mc = Minecraft.getMinecraft();

            HudElement element = HudPositionManager.get("PetHUD");
            int x = element != null ? element.x : 10;
            int y = element != null ? element.y : 30;

            if(element == null) HudPositionManager.registerElement(new HudElement("PetHUD", 10, 30));

            String display = "Pet Ativo: §6" + SkyBlockMod.currentPetName + " §7(Lvl " + SkyBlockMod.currentPetlevel + ")";
            messageDisplayHUDPet = display;

            mc.fontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
        }
    }

    public void drawRect(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void parseAndDisplayPet(String name) {
        try {
            String clean = net.minecraft.util.StringUtils.stripControlCodes(name);
            if (!clean.startsWith("[Lvl") || !clean.contains("'s")) return;

            String level = clean.substring(clean.indexOf("[Lvl") + 4, clean.indexOf("]"));

            String[] split = name.split("'s ");
            if (split.length < 2) return;

            String petName = split[1];

            SkyBlockMod.currentPetlevel = level;
            SkyBlockMod.currentPetName = petName;
        } catch (Exception ignored) {
            SkyBlockMod.currentPetlevel = "0";
            SkyBlockMod.currentPetName = "§cSem Pet Equipado";
        }
    }
}
