package com.vitorxp.WorthClient.gui.utils;

import com.vitorxp.WorthClient.gui.GuiModMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager; // Importante
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationRenderer {

    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public enum Type {
        SUCCESS(0xFF55FF55, "Sucesso"),
        WARNING(0xFFFFAA00, "Aviso"),
        ERROR(0xFFFF5555, "Erro"),
        INFO(0xFFFFFFFF, "Info");

        int color;
        String title;
        Type(int color, String title) { this.color = color; this.title = title; }
    }

    public static void send(Type type, String message) {
        notifications.add(new Notification(type, message));
    }

    public static void render(Minecraft mc) {
        if (notifications.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();

        GlStateManager.translate(0, 0, 1000);

        int screenWidth = mc.displayWidth / mc.gameSettings.guiScale;
        int screenHeight = mc.displayHeight / mc.gameSettings.guiScale;
        int y = 10;

        for (Notification notif : notifications) {
            notif.draw(mc, screenWidth, y);
            y += 35;

            if (notif.isFinished()) {
                notifications.remove(notif);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    static class Notification {
        private final Type type;
        private final String message;
        private final long start;
        private final long duration = 2500;

        public Notification(Type type, String message) {
            this.type = type;
            this.message = message;
            this.start = System.currentTimeMillis();
        }

        public boolean isFinished() {
            return System.currentTimeMillis() > start + duration;
        }

        public void draw(Minecraft mc, int screenWidth, int y) {
            long timeAlive = System.currentTimeMillis() - start;
            FontRenderer fr = mc.fontRendererObj;

            double offset = 0;
            if (timeAlive < 250) {
                offset = 150 * (1 - (timeAlive / 250.0));
            } else if (timeAlive > duration - 250) {
                offset = 150 * (1 - ((duration - timeAlive) / 250.0));
            }

            int width = 140;
            int height = 30;
            int x = (int) (screenWidth - width - 10 + offset);

            GuiModMenu.drawRoundedRect(x, y, width, height, 4, 0xD0151515);
            GuiModMenu.drawRoundedRect(x, y + 2, 3, height - 4, 1, type.color);
            fr.drawStringWithShadow(type.title, x + 10, y + 5, type.color);
            fr.drawString(message, x + 10, y + 17, 0xDDDDDD);
        }
    }
}