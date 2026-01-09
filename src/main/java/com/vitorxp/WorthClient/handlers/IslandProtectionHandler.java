package com.vitorxp.WorthClient.handlers;

import com.vitorxp.WorthClient.WorthClient;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import static com.vitorxp.WorthClient.utils.RankUtils.isStaffM;

public class IslandProtectionHandler {

    private boolean wasOnIsland = false;
    private long lastWarningTime = 0;
    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.ClientTickEvent.Phase.START || mc.thePlayer == null || mc.theWorld == null) return;

        if (!isStaffM(mc.thePlayer)) return;

        boolean nowOnIsland = isScoreboardSuaIlha();

        if (nowOnIsland && !wasOnIsland) {
            if (WorthClient.buildEnabled) {
                WorthClient.buildEnabled = false;
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.GOLD + "[WorthProtection] " + EnumChatFormatting.YELLOW + "Você entrou na sua ilha. Construção BLOQUEADA por segurança."
                ));
            }
        }

        wasOnIsland = nowOnIsland;
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent event) {
        if (!isStaffM(mc.thePlayer)) return;

        if (isScoreboardSuaIlha() && !WorthClient.buildEnabled) {

            event.setCanceled(true);

            if (event.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
                mc.playerController.resetBlockRemoving();
            }

            long now = System.currentTimeMillis();
            if (now - lastWarningTime > 1500) {
                mc.thePlayer.addChatMessage(new ChatComponentText(
                        EnumChatFormatting.RED + "Proteção Ativa! Use " +
                                EnumChatFormatting.YELLOW + "/buildis" +
                                EnumChatFormatting.RED + " para desbloquear."
                ));
                lastWarningTime = now;
            }
        }
    }

    private boolean isScoreboardSuaIlha() {
        if (mc.theWorld == null) return false;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = null;

        if (scoreboard != null) {
            objective = scoreboard.getObjectiveInDisplaySlot(1);
        }

        if (objective == null) return false;

        String title = objective.getDisplayName();
        String cleanTitle = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(title);

        if (cleanTitle != null && cleanTitle.toLowerCase().contains("sua ilha")) {
            return true;
        }

        java.util.Collection<net.minecraft.scoreboard.Score> scores = scoreboard.getSortedScores(objective);

        for (net.minecraft.scoreboard.Score score : scores) {
            String playerName = score.getPlayerName();

            net.minecraft.scoreboard.ScorePlayerTeam team = scoreboard.getPlayersTeam(playerName);
            String lineText = net.minecraft.scoreboard.ScorePlayerTeam.formatPlayerName(team, playerName);

            String cleanLine = net.minecraft.util.EnumChatFormatting.getTextWithoutFormattingCodes(lineText);

            if (cleanLine != null && cleanLine.toLowerCase().contains("sua ilha")) {
                return true;
            }
        }

        return false;
    }
}