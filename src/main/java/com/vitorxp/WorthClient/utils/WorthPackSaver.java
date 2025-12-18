package com.vitorxp.WorthClient.utils;

import com.vitorxp.WorthClient.gui.WorthPackEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

public class WorthPackSaver {

    public static void savePackList(List<WorthPackEntry> visualList) {
        Minecraft mc = Minecraft.getMinecraft();

        mc.gameSettings.resourcePacks.clear();

        for (WorthPackEntry we : visualList) {
            ResourcePackRepository.Entry repoEntry = we.getRepoEntry();

            if (isFolder(repoEntry)) continue;

            String finalName = repoEntry.getResourcePackName();
            File file = getFileFromEntry(repoEntry);
            if (file != null) {
                finalName = file.getName();
            }

            if (!finalName.equals("Default") && !finalName.equals("default")) {
                mc.gameSettings.resourcePacks.add(finalName);
            }
        }

        mc.gameSettings.saveOptions();
    }

    private static File getFileFromEntry(ResourcePackRepository.Entry entry) {
        try {
            for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
                if (f.getType().equals(File.class)) {
                    f.setAccessible(true);
                    return (File) f.get(entry);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static boolean isFolder(ResourcePackRepository.Entry entry) {
        try {
            for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
                if (f.getType().equals(IResourcePack.class)) {
                    f.setAccessible(true);
                    IResourcePack pack = (IResourcePack) f.get(entry);
                    return pack instanceof FolderResourcePack;
                }
            }
        } catch (Exception e) { }
        return false;
    }
}