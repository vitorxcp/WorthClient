package com.vitorxp.WorthClient.gui.utils;

import com.vitorxp.WorthClient.gui.GuiModMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationRenderer {

    private static final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public enum Type {
        SUCCESS(0xFF55FF55, "Sucesso"),
        WARNING(0xFFFFAA00, "Aviso"),
        ERROR(0xFFFF5555, "Erro"),
        INFO(0xFFAAAAAA, "Info");

        int color;
        String title;
        Type(int color, String title) { this.color = color; this.title = title; }
    }

    public static void send(Type type, String message) {
        notifications.add(new Notification(type, message));
    }

    public static void render(Minecraft mc) {
        if (notifications.isEmpty()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        int screenWidth = sr.getScaledWidth();
        int screenHeight = sr.getScaledHeight();

        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();

        int y = 10;

        for (Notification notif : notifications) {
            notif.draw(mc, screenWidth, y);

            double progress = notif.getAnimationProgress();
            if (progress > 0.5 && notif.isExiting()) {
                y += (int) ((notif.height + 5) * (1.0 - notif.getExitProgress()));
            } else {
                y += (notif.height + 5);
            }

            if (notif.isFinished()) {
                notifications.remove(notif);
            }
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    static class Notification {
        private final Type type;
        private final String message;
        private final long start;
        private final long duration = 3000;

        public int width;
        public int height = 28;

        public Notification(Type type, String message) {
            this.type = type;
            this.message = message;
            this.start = System.currentTimeMillis();
        }

        public boolean isFinished() {
            return System.currentTimeMillis() > start + duration;
        }

        public boolean isExiting() {
            return System.currentTimeMillis() > start + duration - 500;
        }

        public double getExitProgress() {
            long timeAlive = System.currentTimeMillis() - start;
            if (timeAlive > duration - 500) {
                return (timeAlive - (duration - 500)) / 500.0;
            }
            return 0;
        }

        public double getAnimationProgress() {
            long timeAlive = System.currentTimeMillis() - start;
            if (timeAlive < 250) {
                return timeAlive / 250.0;
            } else if (timeAlive > duration - 500) {
                return 1.0 + ((timeAlive - (duration - 500)) / 500.0);
            }
            return 0.5;
        }

        public void draw(Minecraft mc, int screenWidth, int y) {
            FontRenderer fr = mc.fontRendererObj;
            long timeAlive = System.currentTimeMillis() - start;

            int titleW = fr.getStringWidth(type.title);
            int msgW = fr.getStringWidth(message);
            this.width = Math.max(titleW, msgW) + 40;
            if (this.width < 140) this.width = 140;

            double offset = 0;
            if (timeAlive < 250) {
                double progress = timeAlive / 250.0;
                offset = this.width * (1 - Math.pow(progress, 0.3));
            }
            else if (timeAlive > duration - 500) {
                double progress = (timeAlive - (duration - 500)) / 500.0;
                offset = this.width * Math.pow(progress, 2);
            }

            int x = (int) (screenWidth - width - 10 + offset);

            GuiModMenu.drawRoundedRect(x, y, width, height, 5, 0xF0181818);
            GuiModMenu.drawRoundedOutline(x, y, width, height, 5, 1.0f, 0x40FFFFFF);

            double timeP = 1.0 - ((double)timeAlive / duration);
            if (timeP > 0) {
                GuiModMenu.drawRoundedRect(x + 2, y + height - 3, (int)((width - 4) * timeP), 1, 0, type.color);
            }

            drawIcon(x + 5, y + 5, type);

            fr.drawString(type.title, x + 25, y + 4, type.color);
            fr.drawString(message, x + 25, y + 15, 0xCCCCCC);
        }

        private void drawIcon(int x, int y, Type type) {
            int c = type.color;
            GuiModMenu.drawCircleSector(x + 8, y + 8, 6, 0, 360);
            GuiModMenu.drawRoundedRect(x + 4, y + 4, 8, 8, 4, c);
        }
    }
}