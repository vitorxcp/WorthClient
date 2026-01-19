package com.vitorxp.WorthClient.gui;

import com.google.common.collect.Lists;
import com.vitorxp.WorthClient.gui.button.GuiModernButton;
import com.vitorxp.WorthClient.interfaces.IResourcePackRepository;
import com.vitorxp.WorthClient.utils.FolderResourcePack;
import com.vitorxp.WorthClient.utils.WorthPackFavorites;
import com.vitorxp.WorthClient.utils.WorthPackSaver;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiScreenWorthPacks extends GuiScreen {

    private static final ResourceLocation BACKGROUND_BLUR = new ResourceLocation("worthclient", "textures/gui/Background_3.png");
    private final GuiScreen parentScreen;
    private GuiWorthPackList availableList;
    private GuiWorthPackList selectedList;
    private ResourcePackRepository packRepository;
    private boolean changed = false;
    private boolean applyPending = false;
    private int applyTicks = 0;

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

            boolean aFav = WorthPackFavorites.isFavorite(a.getResourcePackName());
            boolean bFav = WorthPackFavorites.isFavorite(b.getResourcePackName());
            if (aFav && !bFav) return -1; if (!aFav && bFav) return 1;

            return a.getResourcePackName().compareTo(b.getResourcePackName());
        });

        for (ResourcePackRepository.Entry entry : available) this.availableList.addEntry(new WorthPackEntry(this, entry));
        for (ResourcePackRepository.Entry entry : selected) this.selectedList.addEntry(new WorthPackEntry(this, entry));
    }

    public int getAvailableListWidth() { return this.availableList.getListWidth(); }
    public int getSelectedListWidth() { return this.selectedList.getListWidth(); }

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

    private File getFileFromEntry(ResourcePackRepository.Entry entry) {
        try {
            for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
                if (f.getType().equals(File.class)) {
                    f.setAccessible(true);
                    return (File) f.get(entry);
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
        for (WorthPackEntry we : this.selectedList.getEntries()) {
            newSelected.add(we.getRepoEntry());
        }
        this.packRepository.setRepositories(newSelected);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (applyPending) return;

        if (button.id == 2) {
            File resourcePackDir = this.packRepository.getDirResourcepacks();
            if (java.awt.Desktop.isDesktopSupported()) java.awt.Desktop.getDesktop().open(resourcePackDir);
            else org.lwjgl.Sys.openURL("file://" + resourcePackDir.getAbsolutePath());
        } else if (button.id == 1) {
            if (this.changed) {
                this.applyPending = true;
                this.applyTicks = 0;
            } else {
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    private void applyChangesAndExit() {
        WorthPackSaver.savePackList(this.selectedList.getEntries());
        this.mc.gameSettings.resourcePacks.clear();

        List<String> packsToSave = new ArrayList<>();
        for (WorthPackEntry we : this.selectedList.getEntries()) {
            ResourcePackRepository.Entry entry = we.getRepoEntry();
            if (isFolder(entry)) continue;
            String saveName;
            File file = getFileFromEntry(entry);
            if (file != null) saveName = file.getName();
            else saveName = entry.getResourcePackName();

            if (!saveName.equals("Default") && !saveName.equalsIgnoreCase("default")) {
                packsToSave.add(saveName);
            }
        }
        this.mc.gameSettings.resourcePacks.addAll(packsToSave);
        this.mc.gameSettings.saveOptions();
        this.mc.refreshResources();
        this.applyPending = false;
        this.mc.displayGuiScreen(this.parentScreen);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GuiClientMainMenu.Theme theme = GuiClientMainMenu.currentTheme;
        int accentColor = theme.accentColor.getRGB();
        int textColor = theme.textColor.getRGB();

        if (applyPending) {
            drawDefaultBackground();
            drawApplyingScreen(theme);
            applyTicks++;
            if (applyTicks > 2) {
                applyChangesAndExit();
            }
            return;
        }

        drawDefaultBackground();
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0, 2.0, 2.0);
        this.drawCenteredString(this.fontRendererObj, "Worth Packs", (int)(this.width / 2 / 2.0f), 10, textColor);
        GlStateManager.popMatrix();

        int topMargin = 40;
        int panelWidth = this.availableList.getListWidth();
        int panelHeight = this.availableList.bottom - this.availableList.top;
        int panelBg = 0x80000000;

        drawRoundedRect(this.availableList.left, topMargin, panelWidth, panelHeight, 10, panelBg);
        drawRoundedOutline(this.availableList.left, topMargin, panelWidth, panelHeight, 10, 1.0f, accentColor);
        this.drawCenteredString(this.fontRendererObj, "Disponíveis", this.availableList.left + (panelWidth / 2), topMargin - 12, 0xAAAAAA);
        drawRoundedRect(this.selectedList.left, topMargin, panelWidth, panelHeight, 10, panelBg);
        drawRoundedOutline(this.selectedList.left, topMargin, panelWidth, panelHeight, 10, 1.0f, accentColor);
        this.drawCenteredString(this.fontRendererObj, "Selecionados", this.selectedList.left + (panelWidth / 2), topMargin - 12, 0xAAAAAA);

        this.availableList.drawScreen(mouseX, mouseY, partialTicks);
        this.selectedList.drawScreen(mouseX, mouseY, partialTicks);

        for (GuiButton button : this.buttonList) {
            if (button instanceof GuiModernButton) ((GuiModernButton) button).drawButton(this.mc, mouseX, mouseY, 1.0f);
            else button.drawButton(this.mc, mouseX, mouseY);
        }
    }

    private void drawApplyingScreen(GuiClientMainMenu.Theme theme) {
        drawRect(0, 0, this.width, this.height, 0xCC000000);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5, 1.5, 1.5);
        this.drawCenteredString(this.fontRendererObj, "Aplicando Alterações...", (int)(centerX / 1.5), (int)((centerY - 20) / 1.5), theme.textColor.getRGB());
        GlStateManager.popMatrix();

        int barWidth = 220;
        int barHeight = 10;
        int barX = centerX - (barWidth / 2);
        int barY = centerY + 10;
        int barBg = 0x80404040;
        int barFill = theme.accentColor.getRGB();

        drawRoundedRect(barX, barY, barWidth, barHeight, 3, barBg);
        drawRoundedRect(barX, barY, barWidth, barHeight, 3, barFill);
    }

    @Override
    public void drawDefaultBackground() {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(BACKGROUND_BLUR);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, this.width, this.height, this.width, this.height);

        drawRect(0, 0, this.width, this.height, GuiClientMainMenu.currentTheme.overlayColor.getRGB());
    }

    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float x1 = x; float y1 = y; float x2 = x + width; float y2 = y + height;
        float a = (color >> 24 & 255) / 255.0F; // Alpha corrigido
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
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

    public static void drawRoundedOutline(float x, float y, float width, float height, float radius, float thickness, int color) {
        float x1 = x; float y1 = y; float x2 = x + width; float y2 = y + height;
        float a = (color >> 24 & 255) / 255.0F; // Alpha corrigido
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, a);
        GL11.glLineWidth(thickness);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

        for (int i = 270; i >= 180; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0.0D).endVertex();
        }
        for (int i = 180; i >= 90; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y1 + radius + Math.cos(angle) * radius, 0.0D).endVertex();
        }
        for (int i = 90; i >= 0; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x2 - radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0.0D).endVertex();
        }
        for (int i = 360; i >= 270; i -= 5) {
            double angle = Math.toRadians(i);
            worldrenderer.pos(x1 + radius + Math.sin(angle) * radius, y2 - radius + Math.cos(angle) * radius, 0.0D).endVertex();
        }

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

    @Override public void handleMouseInput() throws IOException {
        if(applyPending) return;
        super.handleMouseInput();
        this.availableList.handleMouseInput();
        this.selectedList.handleMouseInput();
    }
    @Override protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if(applyPending) return;
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.availableList.mouseClicked(mouseX, mouseY, mouseButton);
        this.selectedList.mouseClicked(mouseX, mouseY, mouseButton);
    }
    @Override protected void mouseReleased(int mouseX, int mouseY, int state) {
        if(applyPending) return;
        super.mouseReleased(mouseX, mouseY, state);
        this.availableList.mouseReleased(mouseX, mouseY, state);
        this.selectedList.mouseReleased(mouseX, mouseY, state);
    }
}