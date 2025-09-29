package com.vitorxp.SkyBlockModVX.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.StringUtils;

import java.util.Collection;

public class RankUtils {
    private static final String[] STAFF_LIST = {
            "ADMIN", "MODERADOR", "DEV", "GERENTE", "MASTER", "AJUDANTE"
    };

    private static final String[] STAFF_LISTM = {
            "ADMIN", "DEV", "GERENTE", "MASTER"
    };

    public static boolean isStaff(EntityPlayer player) {
        if(player == null) return false;

        String prefix = player.getDisplayName().getFormattedText();
        if(containsStaff(prefix)) return true;

        NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(player.getUniqueID());
        if(info != null && info.getDisplayName() != null) {
            String tabName = info.getDisplayName().getFormattedText();
            if(containsStaff(tabName)) return true;
        }

        Scoreboard sb = Minecraft.getMinecraft().theWorld.getScoreboard();
        for (ScoreObjective obj : sb.getScoreObjectives()) {
            Collection<Score> scores = sb.getSortedScores(obj);
            for (Score s : scores) {
                String line = s.getPlayerName();
                if(containsStaff(line)) return true;
            }
        }
        return false;
    }

    public static boolean isStaffM(EntityPlayer player) {
        if(player == null) return false;

        String prefix = player.getDisplayName().getFormattedText();
        if(containsStaffM(prefix)) return true;

        NetworkPlayerInfo info = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(player.getUniqueID());
        if(info != null && info.getDisplayName() != null) {
            String tabName = info.getDisplayName().getFormattedText();
            if(containsStaffM(tabName)) return true;
        }

        Scoreboard sb = Minecraft.getMinecraft().theWorld.getScoreboard();
        for (ScoreObjective obj : sb.getScoreObjectives()) {
            Collection<Score> scores = sb.getSortedScores(obj);
            for (Score s : scores) {
                String line = s.getPlayerName();
                if(containsStaffM(line)) return true;
            }
        }
        return false;
    }

    private static boolean containsStaff(String text) {
        if(text == null) return false;
        text = StringUtils.stripControlCodes(text).toUpperCase();
        for  (String staff : STAFF_LIST) {
            if(text.contains("[" + staff) || text.contains(staff + "]") || text.contains(staff)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsStaffM(String text) {
        if(text == null) return false;
        text = StringUtils.stripControlCodes(text).toUpperCase();
        for  (String staff : STAFF_LISTM) {
            if(text.contains("[" + staff) || text.contains(staff + "]") || text.contains(staff)) {
                return true;
            }
        }
        return false;
    }
}
