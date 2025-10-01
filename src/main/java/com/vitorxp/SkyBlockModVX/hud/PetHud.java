package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;

import static com.vitorxp.SkyBlockModVX.manager.ActivationManager.isActivated;

public class PetHud extends HudElement {

    public static String messageDisplayHUDPet = "Pet: §cNenhum";
    private String currentPetName = "§cSem Pet";
    private String currentPetLevel = "0";

    public PetHud() {
        super("PetHUD", 10, 30);
    }

    @Override
    public void update(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.ticksExisted % 20 != 0 || SkyBlockMod.petDisplayViewOff && isActivated) {
            return;
        }

        boolean petFound = false;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityArmorStand)) continue;

            String name = entity.getCustomNameTag();
            if (name == null || name.isEmpty()) continue;

            String cleanName = StringUtils.stripControlCodes(name);

            if (cleanName.startsWith("[Lvl") && cleanName.contains(mc.thePlayer.getName() + "'s")) {
                parsePetInfo(name);
                petFound = true;
                break; // Encontrou o pet, não precisa continuar o loop
            }
        }

        if (!petFound) {
            this.currentPetName = "§cSem Pet";
            this.currentPetLevel = "0";
        }
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!SkyBlockMod.petOverlay) return;

        String display = "Pet: §6" + this.currentPetName + " §7(Lvl " + this.currentPetLevel + ")";
        messageDisplayHUDPet = display;
        fontRenderer.drawStringWithShadow(display, this.x, this.y, 0xFFFFFF);
    }

    private void parsePetInfo(String rawName) {
        try {
            String clean = StringUtils.stripControlCodes(rawName);
            this.currentPetLevel = clean.substring(clean.indexOf("[Lvl ") + 5, clean.indexOf("]"));
            this.currentPetName = rawName.split("'s ")[1];
        } catch (Exception e) {
            this.currentPetName = "§cErro";
            this.currentPetLevel = "0";
        }
    }

    @Override
    public int getWidth() {
        return fontRenderer.getStringWidth(messageDisplayHUDPet);
    }

    @Override
    public int getHeight() {
        return fontRenderer.FONT_HEIGHT;
    }
}