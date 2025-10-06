package com.vitorxp.WorthClient.hud;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HudManager {

    private final Map<String, HudElement> elements = new HashMap<>();

    public void register(HudElement... elementsToAdd) {
        for (HudElement element : elementsToAdd) {
            this.elements.put(element.id, element);
        }
    }

    public Collection<HudElement> getElements() {
        return elements.values();
    }

    // Evento de Renderização Principal (para texto)
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text event) {
        for (HudElement element : elements.values()) {
            element.render(event);
        }
    }

    // NOVO: Evento de Renderização "Post" (para itens)
    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        for (HudElement element : elements.values()) {
            element.renderPost(event);
        }
    }

    // Evento de Tick (para lógica)
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (HudElement element : elements.values()) {
                element.update(event);
            }
        }
    }

    // NOVO: Evento de Mouse (para o Keystrokes)
    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        for (HudElement element : elements.values()) {
            element.onMouse(event);
        }
    }
}