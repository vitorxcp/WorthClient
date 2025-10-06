package com.vitorxp.WorthClient.hud;

import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.ArrayList;
import java.util.List;

public class KeystrokesManager {

    private final List<Long> leftClickTimestamps = new ArrayList<>();
    private final List<Long> rightClickTimestamps = new ArrayList<>();
    private int cpsLeft = 0;
    private int cpsRight = 0;

    public int getCpsLeft() { return cpsLeft; }
    public int getCpsRight() { return cpsRight; }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.keystrokesOverlay) return;

        long now = System.currentTimeMillis();
        leftClickTimestamps.removeIf(timestamp -> now - timestamp > 1000);
        rightClickTimestamps.removeIf(timestamp -> now - timestamp > 1000);

        cpsLeft = leftClickTimestamps.size();
        cpsRight = rightClickTimestamps.size();
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (!com.vitorxp.WorthClient.WorthClient.keystrokesOverlay || !event.buttonstate) return;

        if (event.button == 0) leftClickTimestamps.add(System.currentTimeMillis());
        if (event.button == 1) rightClickTimestamps.add(System.currentTimeMillis());
    }
}