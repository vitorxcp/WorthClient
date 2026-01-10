package com.vitorxp.WorthClient.manager;

import com.vitorxp.WorthClient.gui.GuiSocial;
import com.vitorxp.WorthClient.social.SocialManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {

    private static final List<Notification> notifications = new ArrayList<>();

    public static void show(String fromNick, String message) {
        if ("ocupado".equalsIgnoreCase(SocialManager.myStatus)) return;

        notifications.add(new Notification(fromNick, message));

        try {
            Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
        } catch (Exception ignored) {}
    }

    public static boolean checkClick(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        if (notifications.isEmpty()) return false;

        ScaledResolution sr = new ScaledResolution(mc);
        int y = sr.getScaledHeight() - 40;
        int x = sr.getScaledWidth();

        for (Notification notif : notifications) {
            int notifX = x - notif.width - 5;

            if (mouseX >= notifX && mouseX <= notifX + notif.width &&
                    mouseY >= y && mouseY <= y + notif.height) {

                mc.displayGuiScreen(new GuiSocial(notif.senderNick));
                notifications.remove(notif);
                return true;
            }
            y -= 35;
        }
        return false;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat)) {}
        if (notifications.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();

        ScaledResolution sr = new ScaledResolution(mc);
        int y = sr.getScaledHeight() - 40;
        int x = sr.getScaledWidth();

        for (int i = 0; i < notifications.size(); i++) {
            Notification notif = notifications.get(i);
            notif.render(x, y);

            if (notif.shouldRemove()) {
                notifications.remove(i);
                i--;
            }
            y -= 35;
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    static class Notification {
        public final String senderNick;
        private final String message;
        private final long startTime;
        private final int displayTime = 5000;

        public final int width = 160;
        public final int height = 30;

        public Notification(String sender, String message) {
            this.senderNick = sender;
            this.message = message;
            this.startTime = System.currentTimeMillis();
        }

        public void render(int screenWidth, int y) {
            long timeElapsed = System.currentTimeMillis() - startTime;

            double offset;
            if (timeElapsed < 300) {
                double progress = timeElapsed / 300.0;
                offset = width * (1.0 - Math.pow(progress, 0.5));
            } else if (timeElapsed > displayTime - 300) {
                double progress = (timeElapsed - (displayTime - 300)) / 300.0;
                offset = width * Math.pow(progress, 2);
            } else {
                offset = 0;
            }

            int x = (int) (screenWidth - width - 5 + offset);

            Gui.drawRect(x, y, x + width, y + height, 0xFF18181B);
            Gui.drawRect(x, y, x + 2, y + height, 0xFFEAB308);

            Minecraft mc = Minecraft.getMinecraft();
            mc.fontRendererObj.drawStringWithShadow(senderNick, x + 8, y + 5, 0xFFEAB308);
            mc.fontRendererObj.drawStringWithShadow(limitString(message, 24), x + 8, y + 16, 0xFFA1A1AA);

            double progress = 1.0 - ((double) timeElapsed / displayTime);
            if (progress < 0) progress = 0;
            int barWidth = (int) ((width - 2) * progress);

            Gui.drawRect(x + 2, y + height - 1, x + 2 + barWidth, y + height, 0xFFEAB308);
        }

        public boolean shouldRemove() {
            return System.currentTimeMillis() - startTime > displayTime;
        }

        private String limitString(String s, int maxChars) {
            if (s == null) return "";
            if (s.length() <= maxChars) return s;
            return s.substring(0, maxChars) + "...";
        }
    }
}