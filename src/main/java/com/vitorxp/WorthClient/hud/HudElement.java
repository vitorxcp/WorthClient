package com.vitorxp.WorthClient.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.awt.Point;

public abstract class HudElement {

    protected final Minecraft mc;
    protected final FontRenderer fontRenderer;
    public final String id;
    public int x, y;

    public boolean dragging;
    public int dragOffsetX, dragOffsetY;

    public HudElement(String id, int defaultX, int defaultY) {
        this.id = id;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = this.mc.fontRendererObj;

        Point savedPosition = HudPositionManager.getPosition(id);
        if (savedPosition != null) {
            this.x = savedPosition.x;
            this.y = savedPosition.y;
        } else {
            this.x = defaultX;
            this.y = defaultY;
            HudPositionManager.savePosition(this);
        }
    }

    public abstract int getWidth();
    public abstract int getHeight();

    public void render(RenderGameOverlayEvent event) {}
    public void renderPost(RenderGameOverlayEvent.Post event) {}
    public void update(TickEvent.ClientTickEvent event) {}
    public void onMouse(MouseEvent event) {}
}