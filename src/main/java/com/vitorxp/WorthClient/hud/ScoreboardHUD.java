package com.vitorxp.WorthClient.hud;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import java.util.Collection;
import java.util.List;

public class ScoreboardHUD extends HudElement {

    public static boolean toggled = true;
    public static boolean showNumbers = true;
    public static boolean background = true;
    public static boolean border = false;
    public static int backgroundColor = 0x50000000;
    public static int borderColor = 0xFF000000;
    public static float scale = 1.0f;
    private static final int padding = 4;
    private int cachedWidth = 0;
    private int cachedHeight = 0;

    public ScoreboardHUD() {
        super("Scoreboard", 0, 0);
    }

    @Override
    public void render(RenderGameOverlayEvent event) {
        if (!toggled) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) return;

        Scoreboard scoreboard = mc.theWorld.getScoreboard();
        ScoreObjective objective = null;
        ScorePlayerTeam team = scoreboard.getPlayersTeam(mc.thePlayer.getName());

        if (team != null) {
            int i = team.getChatFormat().getColorIndex();
            if (i >= 0) objective = scoreboard.getObjectiveInDisplaySlot(3 + i);
        }

        ScoreObjective objective1 = objective != null ? objective : scoreboard.getObjectiveInDisplaySlot(1);

        if (objective1 != null) {
            renderScoreboard(objective1);
        }
    }

    private void renderScoreboard(ScoreObjective objective) {
        Minecraft mc = Minecraft.getMinecraft();
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>() {
            public boolean apply(Score p_apply_1_) {
                return p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#");
            }
        }));

        if (list.size() > 15) {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        } else {
            collection = list;
        }

        int maxWidth = mc.fontRendererObj.getStringWidth(objective.getDisplayName());

        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();

            if (!showNumbers) {
                s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
            }

            maxWidth = Math.max(maxWidth, mc.fontRendererObj.getStringWidth(s));
        }

        int lineHeight = mc.fontRendererObj.FONT_HEIGHT;
        int listHeight = collection.size() * lineHeight;
        int titleHeight = lineHeight + 2;
        int totalContentWidth = maxWidth + (padding * 2);
        int totalContentHeight = listHeight + titleHeight;

        this.cachedWidth = (int) (totalContentWidth * scale);
        this.cachedHeight = (int) (totalContentHeight * scale);

        if (this.x == 0 && this.y == 0) {
            ScaledResolution sr = new ScaledResolution(mc);
            this.x = sr.getScaledWidth() - this.cachedWidth - 1;
            this.y = (sr.getScaledHeight() / 2) - (this.cachedHeight / 2);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y, 0);
        GlStateManager.scale(scale, scale, 1.0f);

        String title = objective.getDisplayName();

        if (background) {
            Gui.drawRect(0, 0, totalContentWidth, listHeight + titleHeight, backgroundColor);

            if (border) {
                drawHollowRect(0, 0, totalContentWidth, listHeight + titleHeight, borderColor);
            }
        }

        int titleX = (totalContentWidth / 2) - (mc.fontRendererObj.getStringWidth(title) / 2);
        mc.fontRendererObj.drawString(title, titleX, 2, 0xFFFFFFFF);

        int i = 0;
        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String text = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
            String points = EnumChatFormatting.RED + "" + score.getScorePoints();

            int lineY = titleHeight + (i * lineHeight);

            mc.fontRendererObj.drawString(text, padding, lineY, 0xFFFFFFFF);

            if (showNumbers) {
                mc.fontRendererObj.drawString(points, totalContentWidth - padding - mc.fontRendererObj.getStringWidth(points), lineY, 0xFFFFFFFF);
            }

            i++;
        }

        GlStateManager.popMatrix();
    }

    private void drawHollowRect(int left, int top, int right, int bottom, int color) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    @Override
    public int getWidth() { return this.cachedWidth; }

    @Override
    public int getHeight() { return this.cachedHeight; }
}