package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class WorthPackFavorites {

    private static final File FAVORITES_FILE = new File(Minecraft.getMinecraft().mcDataDir, "worth_favorites.txt");
    private static final Set<String> favorites = new HashSet<>();
    private static boolean loaded = false;

    public static void load() {
        favorites.clear();
        if (!FAVORITES_FILE.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(FAVORITES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    favorites.add(line.trim());
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
        loaded = true;
    }

    public static void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FAVORITES_FILE))) {
            for (String name : favorites) {
                writer.println(name);
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static boolean isFavorite(String name) {
        if (!loaded) load();
        return favorites.contains(stripColors(name));
    }

    public static void toggle(String name) {
        if (!loaded) load();
        String cleanName = stripColors(name);
        if (favorites.contains(cleanName)) {
            favorites.remove(cleanName);
        } else {
            favorites.add(cleanName);
        }
        save();
    }

    private static String stripColors(String input) {
        return input == null ? "" : input.replaceAll("(?i)\\u00A7[0-9a-fklmnor]", "");
    }
}