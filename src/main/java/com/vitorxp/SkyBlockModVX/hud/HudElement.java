package com.vitorxp.SkyBlockModVX.hud;

public class HudElement {
    public String id;
    public int x, y;
    public boolean dragging = false;
    public int dragOffsetX, dragOffsetY;

    public HudElement(String id, int defaultX, int defaultY) {
        this.id = id;
        this.x = defaultX;
        this.y = defaultY;
    }
}