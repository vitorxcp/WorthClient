package com.vitorxp.WorthClient.utils;

import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

public class FolderResourcePack implements IResourcePack {
    private final File folder;
    private final boolean isBack;

    public FolderResourcePack(File folder, boolean isBack) {
        this.folder = folder;
        this.isBack = isBack;
    }

    public File getFolder() { return folder; }

    public boolean isBack() { return isBack; }

    @Override
    public String getPackName() {
        return isBack ? "§c.. (Voltar)" : "§6§l[Pasta] §e" + folder.getName();
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (isBack) {
            g.setColor(new Color(200, 50, 50));
            g.fillRect(16, 24, 32, 16);
            g.fillPolygon(new int[]{4, 16, 16}, new int[]{32, 16, 48}, 3);
        } else {
            g.setColor(new Color(218, 165, 32));
            g.fillRect(8, 16, 48, 40);
            g.fillRect(8, 8, 20, 8);
            g.setColor(new Color(180, 130, 20));
            g.drawRect(8, 16, 48, 40);
        }
        g.dispose();
        return image;
    }

    @Override
    public <T extends IMetadataSection> T getPackMetadata(IMetadataSerializer metadataSerializer, String metadataSectionName) throws IOException {
        if ("pack".equals(metadataSectionName)) {
            String desc = isBack ? "§7Clique para voltar" : "§7Clique para abrir a pasta";
            return (T) new PackMetadataSection(new ChatComponentText(desc), 1);
        }
        return null;
    }

    @Override public InputStream getInputStream(ResourceLocation location) throws IOException { return new ByteArrayInputStream(new byte[0]); }
    @Override public boolean resourceExists(ResourceLocation location) { return false; }
    @Override public Set<String> getResourceDomains() { return Collections.emptySet(); }
}