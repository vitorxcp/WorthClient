package com.vitorxp.WorthClient.gui;

import com.google.common.collect.Lists;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.interfaces.IResourcePackRepository;
import com.vitorxp.WorthClient.utils.FolderResourcePack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class GuiScreenWorthPacks extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");

    private final GuiScreen parentScreen;
    private GuiWorthPackList availableList;
    private GuiWorthPackList selectedList;
    private ResourcePackRepository packRepository;
    private boolean changed = false;

    public GuiScreenWorthPacks(GuiScreen parent) {
        this.parentScreen = parent;
    }

    @Override
    public void initGui() {
        this.buttonList.clear();

        int xCenter = this.width / 2;
        int btnY = this.height - 30;
        int btnWidth = Math.min(150, (this.width / 2) - 20);

        this.buttonList.add(new GuiModernButton(2, xCenter - btnWidth - 5, btnY, btnWidth, 20, "Abrir Pasta", 0L));
        this.buttonList.add(new GuiModernButton(1, xCenter + 5, btnY, btnWidth, 20, "Concluído", 0L));

        this.packRepository = this.mc.getResourcePackRepository();
        this.packRepository.updateRepositoryEntriesAll();

        int topMargin = 40;
        int bottomMargin = 45;
        int sideMargin = 10;
        int middleGap = 10;
        int panelWidth = (this.width - (sideMargin * 2) - middleGap) / 2;
        if (panelWidth > 300) {
            panelWidth = 300;
            sideMargin = (this.width - (panelWidth * 2) - middleGap) / 2;
        }
        int panelHeight = this.height - topMargin - bottomMargin;

        this.availableList = new GuiWorthPackList(this.mc, panelWidth, this.height, topMargin, this.height - bottomMargin, 42);
        this.availableList.setSlotXBoundsFromLeft(sideMargin);

        this.selectedList = new GuiWorthPackList(this.mc, panelWidth, this.height, topMargin, this.height - bottomMargin, 42);
        this.selectedList.setSlotXBoundsFromLeft(sideMargin + panelWidth + middleGap);

        refreshLists();
    }

    public void navigateTo(File folder) {
        if (packRepository instanceof IResourcePackRepository) {
            ((IResourcePackRepository) packRepository).navigateTo(folder);
            this.packRepository.updateRepositoryEntriesAll();
            refreshLists();
        }
    }

    public void refreshLists() {
        this.availableList.clear();
        this.selectedList.clear();

        List<ResourcePackRepository.Entry> all = this.packRepository.getRepositoryEntriesAll();
        List<ResourcePackRepository.Entry> selected = this.packRepository.getRepositoryEntries();
        List<ResourcePackRepository.Entry> available = Lists.newArrayList(all);
        available.removeAll(selected);

        Collections.sort(available, (a, b) -> {
            boolean aIsBack = isBack(a); boolean bIsBack = isBack(b);
            if (aIsBack && !bIsBack) return -1; if (!aIsBack && bIsBack) return 1;
            boolean aIsFolder = isFolder(a); boolean bIsFolder = isFolder(b);
            if (aIsFolder && !bIsFolder) return -1; if (!aIsFolder && bIsFolder) return 1;
            return a.getResourcePackName().compareTo(b.getResourcePackName());
        });

        for (ResourcePackRepository.Entry entry : available) this.availableList.addEntry(new WorthPackEntry(this, entry));
        for (ResourcePackRepository.Entry entry : selected) this.selectedList.addEntry(new WorthPackEntry(this, entry));
    }

    private IResourcePack getPackFromEntry(ResourcePackRepository.Entry entry) {
        try {
            for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
                if (f.getType().equals(IResourcePack.class)) {
                    f.setAccessible(true);
                    return (IResourcePack) f.get(entry);
                }
            }
        } catch (Exception e) { }
        return null;
    }

    private boolean isFolder(ResourcePackRepository.Entry entry) { return getPackFromEntry(entry) instanceof FolderResourcePack; }

    private boolean isBack(ResourcePackRepository.Entry entry) {
        IResourcePack pack = getPackFromEntry(entry);
        return pack instanceof FolderResourcePack && ((FolderResourcePack) pack).getPackName().contains("Voltar");
    }

    public boolean isMouseOver(int x, int y, int mouseX, int mouseY, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isAvailableList(WorthPackEntry entry) { return this.availableList.getEntries().contains(entry); }

    public void selectPack(WorthPackEntry entry) {
        this.changed = true;
        this.availableList.getEntries().remove(entry);
        this.selectedList.addEntry(entry);
        try { entry.getRepoEntry().updateResourcePack(); } catch (IOException e) { e.printStackTrace(); }
        updateMinecraftList();
        refreshLists();
    }

    public void deselectPack(WorthPackEntry entry) {
        this.changed = true;
        this.selectedList.getEntries().remove(entry);
        updateMinecraftList();
        refreshLists();
    }

    private void updateMinecraftList() {
        List<ResourcePackRepository.Entry> newSelected = Lists.newArrayList();
        for (WorthPackEntry we : this.selectedList.getEntries()) newSelected.add(we.getRepoEntry());
        this.packRepository.setRepositories(newSelected);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            File resourcePackDir = this.packRepository.getDirResourcepacks();
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(resourcePackDir);
            else org.lwjgl.Sys.openURL("file://" + resourcePackDir.getAbsolutePath());
        } else if (button.id == 1) {
            if (this.changed) applyChangesWithLoading();
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    private void applyChangesWithLoading() {
        this.mc.gameSettings.resourcePacks.clear();
        for (ResourcePackRepository.Entry entry : this.packRepository.getRepositoryEntries()) {
            this.mc.gameSettings.resourcePacks.add(entry.getResourcePackName());
        }
        this.mc.gameSettings.saveOptions();

        ScaledResolution sr = new ScaledResolution(mc);
        drawRect(0, 0, this.width, this.height, 0xFF101010);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        GlStateManager.color(0.4F, 0.4F, 0.4F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        int boxW = 220; int boxH = 70;
        int boxX = (this.width - boxW) / 2;
        int boxY = (this.height - boxH) / 2;

        drawRoundedRect(boxX, boxY, boxW, boxH, 10, 0xE5000000);

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.2, 1.2, 1.2);
        this.drawCenteredString(this.fontRendererObj, "Aplicando Alterações", (int)(this.width / 2 / 1.2), (int)((boxY + 15) / 1.2), 0xFFFFFF);
        GlStateManager.popMatrix();

        this.drawCenteredString(this.fontRendererObj, "Recarregando recursos...", this.width / 2, boxY + 40, 0xAAAAAA);
        this.drawCenteredString(this.fontRendererObj, "Por favor aguarde.", this.width / 2, boxY + 52, 0x888888);

        Display.update();
        this.mc.refreshResources();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 2.0);
        this.drawCenteredString(this.fontRendererObj, "Worth Packs", (int)(this.width / 2 / 2.0f), 10, 0xFFFFFF);
        GlStateManager.popMatrix();

        int topMargin = 40;
        int panelWidth = this.availableList.getListWidth();
        int panelHeight = this.availableList.bottom - this.availableList.top;

        drawRoundedRect(this.availableList.left, topMargin, panelWidth, panelHeight, 10, 0x80000000);
        this.drawCenteredString(this.fontRendererObj, "Disponíveis", this.availableList.left + (panelWidth / 2), topMargin - 12, 0xAAAAAA);

        drawRoundedRect(this.selectedList.left, topMargin, panelWidth, panelHeight, 10, 0x80000000);
        this.drawCenteredString(this.fontRendererObj, "Selecionados", this.selectedList.left + (panelWidth / 2), topMargin - 12, 0xAAAAAA);

        this.availableList.drawScreen(mouseX, mouseY, partialTicks);
        this.selectedList.drawScreen(mouseX, mouseY, partialTicks);

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, 1.0f);
            else button.drawButton(this.mc, mouseX, mouseY);
        }
    }

    @Override
    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);
        drawRect(0, 0, this.width, this.height, 0x64050505);
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x; float y1 = y; float x2 = x + width; float y2 = y + height;
        float f = (color >> 24 & 255) / 255.0F;
        float f1 = (color >> 16 & 255) / 255.0F;
        float f2 = (color >> 8 & 255) / 255.0F;
        float f3 = (color & 255) / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(f1, f2, f3, f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        drawCircleSector(x1 + radius, y1 + radius, radius, 180, 270);
        drawCircleSector(x2 - radius, y1 + radius, radius, 90, 180);
        drawCircleSector(x2 - radius, y2 - radius, radius, 0, 90);
        drawCircleSector(x1 + radius, y2 - radius, radius, 270, 360);

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        worldrenderer.pos(x1 + radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1, 0.0D).endVertex();
        worldrenderer.pos(x1, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x1 + radius, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x1, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y2 - radius, 0.0D).endVertex();
        worldrenderer.pos(x2, y1 + radius, 0.0D).endVertex();
        worldrenderer.pos(x2 - radius, y1 + radius, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawCircleSector(float cx, float cy, float r, int startAngle, int endAngle) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION);
        worldrenderer.pos(cx, cy, 0.0D).endVertex();
        for (int i = startAngle; i <= endAngle; i++) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(cx + Math.sin(angle) * r, cy + Math.cos(angle) * r, 0.0D).endVertex();
        }
        tessellator.draw();
    }

    @Override public void handleMouseInput() throws IOException { super.handleMouseInput(); this.availableList.handleMouseInput(); this.selectedList.handleMouseInput(); }
    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException { super.mouseClicked(mouseX, mouseY, mouseButton); this.availableList.mouseClicked(mouseX, mouseY, mouseButton); this.selectedList.mouseClicked(mouseX, mouseY, mouseButton); }
    @Override protected void mouseReleased(int mouseX, int mouseY, int state) { super.mouseReleased(mouseX, mouseY, state); this.availableList.mouseReleased(mouseX, mouseY, state); this.selectedList.mouseReleased(mouseX, mouseY, state); }
}