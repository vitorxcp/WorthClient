package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.RadarManager;
import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Rectangle;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.vitorxp.SkyBlockModVX.utils.RankUtils.isStaff;

public class RadarHUD {

    private static final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        renderAllPlayersHUD();
    }

    public static void renderAllPlayersHUD() {
        if (!SkyBlockMod.RadarOverlay || !isStaff(mc.thePlayer)) {
            return;
        }

        HudElement element = HudPositionManager.get("RadarHUD");

        if (element == null) {
            element = new HudElement("RadarHUD", 5, 5);
            HudPositionManager.registerElement(element);
        }

        int x = element.x;
        int y = element.y;

        FontRenderer fr = mc.fontRendererObj;
        int lineHeight = fr.FONT_HEIGHT + 2;

        RadarManager.playerClickAreas.clear();

        fr.drawStringWithShadow("§b-- Radar de Jogadores --", x, y, 0xFFFFFF);

        int titleWidth = fr.getStringWidth("§b-- Radar de Jogadores --");
        RadarManager.playerClickAreas.put("RadarTitle", new Rectangle(x, y, titleWidth, fr.FONT_HEIGHT));

        y += lineHeight;

        NetHandlerPlayClient netHandler = mc.getNetHandler();
        if (netHandler == null) return;

        final java.util.Map<String, net.minecraft.client.network.NetworkPlayerInfo> playerInfoMap =
                netHandler.getPlayerInfoMap().stream()
                        .collect(Collectors.toMap(
                                info -> info.getGameProfile().getName(),
                                info -> info,
                                (existingValue, newValue) -> newValue
                        ));

        List<EntityPlayer> nearbyPlayers = mc.theWorld.playerEntities.stream()
                .filter(player -> {
                    if (player == mc.thePlayer) {
                        return false;
                    }

                    net.minecraft.client.network.NetworkPlayerInfo info = playerInfoMap.get(player.getName());
                    if (info == null) {
                        return false;
                    }

                    return info.getResponseTime() > 0;
                })
                .sorted(Comparator.comparingDouble(player -> mc.thePlayer.getDistanceToEntity(player)))
                .collect(Collectors.toList());

        if (nearbyPlayers.isEmpty()) {
            fr.drawStringWithShadow("§7Nenhum jogador por perto.", x, y, 0xFFFFFF);
        } else {
            for (EntityPlayer player : nearbyPlayers) {
                int distance = (int) mc.thePlayer.getDistanceToEntity(player);
                String playerName = player.getName();
                String text = String.format("§a%s §f- §e%d m", playerName, distance);

                fr.drawStringWithShadow(text, x, y, 0xFFFFFF);

                int textWidth = fr.getStringWidth(text);
                Rectangle clickArea = new Rectangle(x, y, textWidth, fr.FONT_HEIGHT);
                RadarManager.playerClickAreas.put(playerName, clickArea);

                y += lineHeight;
            }
        }
    }
}