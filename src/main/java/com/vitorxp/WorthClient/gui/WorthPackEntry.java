package com.vitorxp.WorthClient.gui;

import com.vitorxp.WorthClient.utils.FolderResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import java.awt.Color;
import java.lang.reflect.Field;
import java.util.List;

public class WorthPackEntry implements GuiListExtended.IGuiListEntry {
    private final ResourcePackRepository.Entry repoEntry;
    private final GuiScreenWorthPacks parentScreen;
    private final Minecraft mc;

    public WorthPackEntry(GuiScreenWorthPacks parent, ResourcePackRepository.Entry entry) {
        this.parentScreen = parent;
        this.repoEntry = entry;
        this.mc = Minecraft.getMinecraft();
    }

    public ResourcePackRepository.Entry getRepoEntry() { return repoEntry; }
    public boolean isFolder() { return getPack() instanceof FolderResourcePack; }

    public IResourcePack getPack() {
        try {
            for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
                if (f.getType().equals(IResourcePack.class)) {
                    f.setAccessible(true);
                    return (IResourcePack) f.get(repoEntry);
                }
            }
        } catch (Exception e) { }
        return null;
    }

    @Override public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) { }

    @Override
    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        IResourcePack pack = getPack();
        if (pack == null) return;

        boolean isHovered = this.parentScreen.isMouseOver(x, y, mouseX, mouseY, listWidth, slotHeight);

        int bgColor = 0x40000000;
        if (isHovered) bgColor = 0x60606060;

        GuiScreenWorthPacks.drawRoundedRect(x + 2, y + 1, listWidth - 10, slotHeight - 2, 5, new Color(bgColor, true).getRGB());

        int iconY = y + (slotHeight - 32) / 2;

        repoEntry.bindTexturePackIcon(this.mc.getTextureManager());
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x + 6, iconY, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);

        String title = repoEntry.getResourcePackName();
        String desc = repoEntry.getTexturePackDescription();

        if (isFolder()) {
            if (title.contains("Voltar")) title = "§c" + title;
            else if (!title.contains("§6")) title = "§6§l[Pasta] §e" + title;
        }

        int maxTextWidth = listWidth - 55;
        String trimmedTitle = this.mc.fontRendererObj.trimStringToWidth(title, maxTextWidth);
        if (trimmedTitle.length() < title.length()) trimmedTitle += "...";

        this.mc.fontRendererObj.drawStringWithShadow(trimmedTitle, x + 44, y + 5, 0xFFFFFF);

        List<String> list = this.mc.fontRendererObj.listFormattedStringToWidth(desc, maxTextWidth);
        for (int i = 0; i < 2 && i < list.size(); ++i) {
            this.mc.fontRendererObj.drawStringWithShadow(list.get(i), x + 44, y + 16 + 10 * i, 0xAAAAAA);
        }
    }

    @Override
    public boolean mousePressed(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
        if (isFolder()) {
            FolderResourcePack folder = (FolderResourcePack) getPack();
            parentScreen.navigateTo(folder.getFolder());
            this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
            return true;
        } else {
            if (parentScreen.isAvailableList(this)) parentScreen.selectPack(this);
            else parentScreen.deselectPack(this);
            this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord.create(new net.minecraft.util.ResourceLocation("gui.button.press"), 1.0F));
            return true;
        }
    }

    @Override public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {}
}