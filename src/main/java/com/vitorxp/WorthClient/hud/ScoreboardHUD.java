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
    private static final int pad = 5;
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

        int totalContentWidth = maxWidth + (pad * 2);

        this.cachedWidth = (int) (totalContentWidth * scale);
        this.cachedHeight = (int) ((listHeight + titleHeight) * scale);

        int l1 = this.x;
        int j1 = this.y;

        GlStateManager.pushMatrix();
        GlStateManager.translate(l1, j1, 0);
        GlStateManager.scale(scale, scale, 1.0f);
        GlStateManager.translate(-l1, -j1, 0);

        String title = objective.getDisplayName();

        if (background) {
            Gui.drawRect(l1, j1 - titleHeight, l1 + totalContentWidth, j1 + listHeight, backgroundColor);

            if (border) {
                drawHollowRect(l1, j1 - titleHeight, l1 + totalContentWidth, j1 + listHeight, borderColor);
            }
        }

        int titleX = l1 + (totalContentWidth / 2) - (mc.fontRendererObj.getStringWidth(title) / 2);
        mc.fontRendererObj.drawString(title, titleX, j1 - lineHeight, 0xFFFFFFFF);

        int i = 0;
        for (Score score : collection) {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String text = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName());
            String points = EnumChatFormatting.RED + "" + score.getScorePoints();

            int lineY = j1 + (collection.size() - i - 1) * lineHeight;

            mc.fontRendererObj.drawString(text, l1 + pad, lineY, 0xFFFFFFFF);

            if (showNumbers) {
                mc.fontRendererObj.drawString(points, l1 + totalContentWidth - pad - mc.fontRendererObj.getStringWidth(points), lineY, 0xFFFFFFFF);
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