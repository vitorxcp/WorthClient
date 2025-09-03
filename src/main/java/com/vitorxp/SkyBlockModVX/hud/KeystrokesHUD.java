package com.vitorxp.SkyBlockModVX.hud;

import com.vitorxp.SkyBlockModVX.SkyBlockMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import com.vitorxp.SkyBlockModVX.config.KeystrokesColors;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class KeystrokesHUD {
    private static final List<Long> leftClickTimestamps = new ArrayList<>();
    private static final List<Long> rightClickTimestamps = new ArrayList<>();

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        if (!SkyBlockMod.keystrokesOverlay) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        HudElement element = HudPositionManager.get("KeystrokesHUD");
        int x = element != null ? element.x : 10;
        int y = element != null ? element.y : 50;
        if (element == null) HudPositionManager.registerElement(new HudElement("KeystrokesHUD", x, y));

        HudElement lmbElement = HudPositionManager.get("KeystrokesLMB");
        int lmbX = lmbElement != null ? lmbElement.x : x + 100;
        int lmbY = lmbElement != null ? lmbElement.y : y;
        if (lmbElement == null) HudPositionManager.registerElement(new HudElement("KeystrokesLMB", lmbX, lmbY));

        HudElement rmbElement = HudPositionManager.get("KeystrokesRMB");
        int rmbX = rmbElement != null ? rmbElement.x : x + 100;
        int rmbY = rmbElement != null ? rmbElement.y : y + 25;
        if (rmbElement == null) HudPositionManager.registerElement(new HudElement("KeystrokesRMB", rmbX, rmbY));

        long now = System.currentTimeMillis();
        removeOldClicks(leftClickTimestamps, now);
        removeOldClicks(rightClickTimestamps, now);

        int cpsLeft = leftClickTimestamps.size();
        int cpsRight = rightClickTimestamps.size();

        int boxSize = 20;
        int padding = 2;
        int spacing = boxSize + padding;

        drawKey(mc, "W", x + spacing, y, Keyboard.isKeyDown(Keyboard.KEY_W));
        drawKey(mc, "A", x, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_A));
        drawKey(mc, "S", x + spacing, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_S));
        drawKey(mc, "D", x + spacing * 2, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_D));

        drawKey(mc, "⬇", x, y + spacing * 2, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), 12, boxSize);
        drawKey(mc, "SPACE", x + 14, y + spacing * 2, Keyboard.isKeyDown(Keyboard.KEY_SPACE), boxSize * 2 + padding + 5, boxSize);

        drawCpsKey(mc, "LMB", cpsLeft, lmbX, lmbY, Mouse.isButtonDown(0));
        drawCpsKey(mc, "RMB", cpsRight, rmbX, rmbY, Mouse.isButtonDown(1));
    }

    private static void removeOldClicks(List<Long> list, long now) {
        Iterator<Long> iterator = list.iterator();
        while (iterator.hasNext()) {
            if (now - iterator.next() > 1000) iterator.remove();
        }
    }

    public static void renderAllItemsHUD() {
        if (!SkyBlockMod.keystrokesOverlay) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        HudElement element = HudPositionManager.get("KeystrokesHUD");
        int x = element != null ? element.x : 10;
        int y = element != null ? element.y : 50;
        if (element == null) HudPositionManager.registerElement(new HudElement("KeystrokesHUD", x, y));

        HudElement lmbElement = HudPositionManager.get("KeystrokesLMB");
        int lmbX = lmbElement != null ? lmbElement.x : x + 100;
        int lmbY = lmbElement != null ? lmbElement.y : y;
        if (lmbElement == null) HudPositionManager.registerElement(new HudElement("KeystrokesLMB", lmbX, lmbY));

        HudElement rmbElement = HudPositionManager.get("KeystrokesRMB");
        int rmbX = rmbElement != null ? rmbElement.x : x + 100;
        int rmbY = rmbElement != null ? rmbElement.y : y + 25;
        if (rmbElement == null) HudPositionManager.registerElement(new HudElement("KeystrokesRMB", rmbX, rmbY));

        long now = System.currentTimeMillis();
        removeOldClicks(leftClickTimestamps, now);
        removeOldClicks(rightClickTimestamps, now);

        int cpsLeft = leftClickTimestamps.size();
        int cpsRight = rightClickTimestamps.size();

        int boxSize = 20;
        int padding = 2;
        int spacing = boxSize + padding;

        drawKey(mc, "W", x + spacing, y, Keyboard.isKeyDown(Keyboard.KEY_W));
        drawKey(mc, "A", x, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_A));
        drawKey(mc, "S", x + spacing, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_S));
        drawKey(mc, "D", x + spacing * 2, y + spacing, Keyboard.isKeyDown(Keyboard.KEY_D));

        drawKey(mc, "⬇", x, y + spacing * 2, Keyboard.isKeyDown(Keyboard.KEY_LSHIFT), 12, boxSize);
        drawKey(mc, "SPACE", x + 14, y + spacing * 2, Keyboard.isKeyDown(Keyboard.KEY_SPACE), boxSize * 2 + padding + 5, boxSize);

        drawCpsKey(mc, "LMB", cpsLeft, lmbX, lmbY, Mouse.isButtonDown(0));
        drawCpsKey(mc, "RMB", cpsRight, rmbX, rmbY, Mouse.isButtonDown(1));
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!SkyBlockMod.keystrokesOverlay) return;

        long now = System.currentTimeMillis();
        if (event.buttonstate) {
            if (event.button == 0) leftClickTimestamps.add(now);
            if (event.button == 1) rightClickTimestamps.add(now);
        }
    }

    private static void drawKey(Minecraft mc, String label, int x, int y, boolean pressed) {
        drawKey(mc, label, x, y, pressed, 20, 20);
    }

    private static void drawCpsKey(Minecraft mc, String label, int cps, int x, int y, boolean pressed) {
        int width = 40;
        int height = 20;
        int bgColor = KeystrokesColors.getBackground(pressed);
        int borderColor = KeystrokesColors.getBorder();

        drawBorderedRect(x, y, x + width, y + height, 1.0F, borderColor, bgColor);

        int labelWidth = mc.fontRendererObj.getStringWidth(label);
        int labelX = x + (width / 2) - (labelWidth / 2);
        int labelY = y + 3;
        mc.fontRendererObj.drawStringWithShadow(label, labelX, labelY, KeystrokesColors.getText());

        String cpsText = cps + " CPS";
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + width / 2.0f, y + height - 6, 0);
        GlStateManager.scale(0.7f, 0.7f, 1f);
        int cpsWidth = mc.fontRendererObj.getStringWidth(cpsText);
        mc.fontRendererObj.drawStringWithShadow(cpsText, -cpsWidth / 2, 0, KeystrokesColors.getCpsText());
        GlStateManager.popMatrix();
    }

    private static void drawKey(Minecraft mc, String label, int x, int y, boolean pressed, int width, int height) {
        int bgColor = KeystrokesColors.getBackground(pressed);
        int borderColor = KeystrokesColors.getBorder();

        drawBorderedRect(x, y, x + width, y + height, 1.0F, borderColor, bgColor);

        String[] lines = label.split("\n");
        for (int i = 0; i < lines.length; i++) {
            int textWidth = mc.fontRendererObj.getStringWidth(lines[i]);
            int textX = x + (width / 2) - (textWidth / 2);
            int textY = y + (height / lines.length) * i + 4;
            mc.fontRendererObj.drawStringWithShadow(lines[i], textX, textY, KeystrokesColors.getText());
        }
    }

    private static void drawBorderedRect(int x1, int y1, int x2, int y2, float borderSize, int borderColor, int insideColor) {
        drawRect(x1, y1, x2, y2, insideColor);
        drawRect(x1, y1, x2, (int) (y1 + borderSize), borderColor);
        drawRect(x1, (int) (y2 - borderSize), x2, y2, borderColor);
        drawRect(x1, (int) (y1 + borderSize), (int) (x1 + borderSize), (int) (y2 - borderSize), borderColor);
        drawRect((int) (x2 - borderSize), (int) (y1 + borderSize), x2, (int) (y2 - borderSize), borderColor);
    }

    private static void drawRect(int left, int top, int right, int bottom, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(left, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(right, top, 0.0D).color(r, g, b, a).endVertex();
        worldrenderer.pos(left, top, 0.0D).color(r, g, b, a).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }
}