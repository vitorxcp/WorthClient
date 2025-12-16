package com.vitorxp.WorthClient.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HudManager {

    private final Map<String, HudElement> elements = new HashMap<>();
    private int lastWidth = -1;
    private int lastHeight = -1;
    private final Map<HudElement, Integer> prevWidths = new HashMap<>();
    private final Map<HudElement, Integer> prevHeights = new HashMap<>();
    private final Map<HudElement, Integer> anchorRight = new HashMap<>();
    private final Map<HudElement, Integer> anchorBottom = new HashMap<>();

    public void register(HudElement... elementsToAdd) {
        for (HudElement element : elementsToAdd) {
            this.elements.put(element.id, element);
        }
    }

    public Collection<HudElement> getElements() {
        return elements.values();
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        int curWidth = event.resolution.getScaledWidth();
        int curHeight = event.resolution.getScaledHeight();

        if (lastWidth != -1 && lastHeight != -1 && (curWidth != lastWidth || curHeight != lastHeight)) {
            float ratioX = (float) curWidth / lastWidth;
            float ratioY = (float) curHeight / lastHeight;

            for (HudElement element : elements.values()) {
                element.x = Math.round(element.x * ratioX);
                element.y = Math.round(element.y * ratioY);
                anchorRight.remove(element);
                anchorBottom.remove(element);
            }
        }
        lastWidth = curWidth;
        lastHeight = curHeight;

        for (HudElement element : elements.values()) {
            element.render(event);

            int w = element.getWidth();
            int h = element.getHeight();

            int prevW = prevWidths.getOrDefault(element, w);
            int prevH = prevHeights.getOrDefault(element, h);

            if (w != prevW) {
                if (anchorRight.containsKey(element)) {
                    int dist = anchorRight.get(element);
                    element.x = curWidth - w - dist;
                } else if (element.x + (prevW / 2) > curWidth / 2) {
                    element.x += (prevW - w);
                }
                prevWidths.put(element, w);
            } else {
                if (element.x >= 0 && element.x + w <= curWidth) {
                    if (element.x + (w / 2) > curWidth / 2) {
                        anchorRight.put(element, curWidth - (element.x + w));
                    } else {
                        anchorRight.remove(element);
                    }
                }
            }

            if (h != prevH) {
                if (anchorBottom.containsKey(element)) {
                    int dist = anchorBottom.get(element);
                    element.y = curHeight - h - dist;
                } else if (element.y + (prevH / 2) > curHeight / 2) {
                    element.y += (prevH - h);
                }
                prevHeights.put(element, h);
            } else {
                if (element.y >= 0 && element.y + h <= curHeight) {
                    if (element.y + (h / 2) > curHeight / 2) {
                        anchorBottom.put(element, curHeight - (element.y + h));
                    } else {
                        anchorBottom.remove(element);
                    }
                }
            }

            validatePosition(element, curWidth, curHeight);
        }
    }

    private void validatePosition(HudElement element, int screenWidth, int screenHeight) {
        int w = element.getWidth();
        int h = element.getHeight();

        if (element.x + w > screenWidth) element.x = screenWidth - w;
        if (element.y + h > screenHeight) element.y = screenHeight - h;
        if (element.x < 0) element.x = 0;
        if (element.y < 0) element.y = 0;
    }

    @SubscribeEvent public void onRenderPost(RenderGameOverlayEvent.Post event) { for (HudElement e : elements.values()) e.renderPost(event); }
    @SubscribeEvent public void onTick(TickEvent.ClientTickEvent event) { if (event.phase == TickEvent.Phase.END) for (HudElement e : elements.values()) e.update(event); }
    @SubscribeEvent public void onMouse(MouseEvent event) { for (HudElement e : elements.values()) e.onMouse(event); }
}