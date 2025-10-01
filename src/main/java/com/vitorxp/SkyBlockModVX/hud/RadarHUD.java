package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import com.vitorxp.SkyBlockModVX.utils.RankUtils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RadarHUD extends HudElement {

    private final List<String> cachedPlayers = new ArrayList<>();
    private int width = 0;
    private int height = 0;

    public RadarHUD() {
        super("RadarHUD", 5, 5);
    }

    @Override
    public void update(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        cachedPlayers.clear();
        if (!SkyBlockMod.RadarOverlay || !RankUtils.isStaff(mc.thePlayer)) {
            return;
        }

        final Map<String, NetworkPlayerInfo> playerInfoMap =
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

        cachedPlayers.add("§b-- Radar de Jogadores --");
        if (nearbyPlayers.isEmpty()) {
            cachedPlayers.add("§7Nenhum jogador por perto.");
        } else {
            for (EntityPlayer player : nearbyPlayers) {
                int distance = (int) mc.thePlayer.getDistanceToEntity(player);
                cachedPlayers.add(String.format("§a%s §f- §e%d m", player.getName(), distance));
            }
        }

        this.width = cachedPlayers.stream().mapToInt(fontRenderer::getStringWidth).max().orElse(0);
        this.height = cachedPlayers.size() * (fontRenderer.FONT_HEIGHT + 2);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!SkyBlockMod.RadarOverlay || !RankUtils.isStaff(mc.thePlayer)) {
            return;
        }

        int currentY = this.y;
        for (String line : cachedPlayers) {
            fontRenderer.drawStringWithShadow(line, this.x, currentY, 0xFFFFFF);
            currentY += fontRenderer.FONT_HEIGHT + 2;
        }
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }
}