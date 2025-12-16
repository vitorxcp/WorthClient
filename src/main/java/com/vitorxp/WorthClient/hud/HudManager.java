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
    private final Map<HudElement, Integer> targetXMap = new HashMap<>();
    private final Map<HudElement, Integer> targetYMap = new HashMap<>();

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

        for (HudElement element : elements.values()) {
            if (!targetXMap.containsKey(element)) targetXMap.put(element, element.x);
            if (!targetYMap.containsKey(element)) targetYMap.put(element, element.y);
        }

        if (lastWidth != -1 && lastHeight != -1 && (curWidth != lastWidth || curHeight != lastHeight)) {
            float ratioX = (float) curWidth / lastWidth;
            float ratioY = (float) curHeight / lastHeight;

            for (HudElement element : elements.values()) {
                int newX = Math.round(targetXMap.get(element) * ratioX);
                int newY = Math.round(targetYMap.get(element) * ratioY);
                targetXMap.put(element, newX);
                targetYMap.put(element, newY);
            }
        }
        lastWidth = curWidth;
        lastHeight = curHeight;

        for (HudElement element : elements.values()) {
            element.render(event);

            int w = element.getWidth();
            int h = element.getHeight();

            int targetX = targetXMap.get(element);
            int targetY = targetYMap.get(element);

            if (element.dragging) {
                targetX = element.x;
                targetY = element.y;
                targetXMap.put(element, targetX);
                targetYMap.put(element, targetY);
            }

            int prevW = prevWidths.getOrDefault(element, w);
            int prevH = prevHeights.getOrDefault(element, h);

            if (w != prevW) {
                if (targetX + (prevW / 2) > curWidth / 2) {
                    targetX += (prevW - w);
                    targetXMap.put(element, targetX);
                }
                prevWidths.put(element, w);
            }

            if (h != prevH) {
                if (targetY + (prevH / 2) > curHeight / 2) {
                    targetY += (prevH - h);
                    targetYMap.put(element, targetY);
                }
                prevHeights.put(element, h);
            }

            element.x = Math.max(0, Math.min(curWidth - w, targetX));
            element.y = Math.max(0, Math.min(curHeight - h, targetY));
        }
    }

    @SubscribeEvent public void onRenderPost(RenderGameOverlayEvent.Post event) { for (HudElement e : elements.values()) e.renderPost(event); }
    @SubscribeEvent public void onTick(TickEvent.ClientTickEvent event) { if (event.phase == TickEvent.Phase.END) for (HudElement e : elements.values()) e.update(event); }
    @SubscribeEvent public void onMouse(MouseEvent event) { for (HudElement e : elements.values()) e.onMouse(event); }
}