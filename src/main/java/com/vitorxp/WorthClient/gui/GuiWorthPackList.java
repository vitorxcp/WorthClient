package com.vitorxp.WorthClient.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiWorthPackList extends GuiListExtended {
    private final List<WorthPackEntry> entries = new ArrayList<>();

    public GuiWorthPackList(Minecraft mc, int width, int height, int top, int bottom, int slotHeight) {
        super(mc, width, height, top, bottom, slotHeight);
        this.showSelectionBox = false;
    }

    public void addEntry(WorthPackEntry entry) { this.entries.add(entry); }
    public void clear() { this.entries.clear(); }
    public List<WorthPackEntry> getEntries() { return this.entries; }

    @Override public IGuiListEntry getListEntry(int index) { return this.entries.get(index); }
    @Override protected int getSize() { return this.entries.size(); }
    @Override public int getListWidth() { return this.width; }
    @Override protected int getScrollBarX() { return this.left + this.width - 6; }

    @Override protected void drawContainerBackground(Tessellator tessellator) { }
    @Override protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha) { }

    @Override
    public void drawScreen(int mouseXIn, int mouseYIn, float p_148128_3_) {
        if (this.field_178041_q) {
            this.mouseX = mouseXIn;
            this.mouseY = mouseYIn;
            this.drawBackground();
            int i = this.getScrollBarX();
            int j = i + 6;
            this.bindAmountScrolled();

            Tessellator tessellator = Tessellator.getInstance();

            ScaledResolution sr = new ScaledResolution(mc);
            int scale = sr.getScaleFactor();
            int scissorX = this.left * scale;
            int scissorY = (mc.displayHeight) - (this.bottom * scale);
            int scissorW = this.width * scale;
            int scissorH = (this.bottom - this.top) * scale;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

            int k = this.left + this.width / 2 - this.getListWidth() / 2 + 2;
            int l = this.top + 4 - (int)this.amountScrolled;

            if (this.hasListHeader) this.drawListHeader(k, l, tessellator);

            this.drawSelectionBox(k, l, mouseXIn, mouseYIn);

            GL11.glDisable(GL11.GL_SCISSOR_TEST);

            int i1 = this.func_148135_f();
            if (i1 > 0) {
                int j1 = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
                j1 = net.minecraft.util.MathHelper.clamp_int(j1, 32, this.bottom - this.top - 8);
                int k1 = (int)this.amountScrolled * (this.bottom - this.top - j1) / i1 + this.top;
                if (k1 < this.top) k1 = this.top;
                GuiScreenWorthPacks.drawRoundedRect(i, k1, 3, j1, 1.5f, 0x80FFFFFF);
            }
            this.func_148142_b(mouseXIn, mouseYIn);
        }
    }

    @Override
    protected void drawSelectionBox(int x, int y, int mouseXIn, int mouseYIn) {
        int i = this.getSize();
        for (int j = 0; j < i; ++j) {
            int k = y + j * this.slotHeight + this.headerPadding;
            int l = this.slotHeight;
            if (k > this.bottom || k + l < this.top) {
                this.func_178040_a(j, x, k);
                continue;
            }
            boolean isSelected = this.getSlotIndexFromScreenCoords(mouseXIn, mouseYIn) == j;
            this.getListEntry(j).drawEntry(j, x, k, this.getListWidth(), l, mouseXIn, mouseYIn, isSelected);
        }
    }
}