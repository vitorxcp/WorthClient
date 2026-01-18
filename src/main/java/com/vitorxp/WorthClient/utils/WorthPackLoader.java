package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorthPackLoader {

    private static boolean firstLoadDone = false;

    public static void reloadSavedPacks() {
        Minecraft mc = Minecraft.getMinecraft();
        ResourcePackRepository repo = mc.getResourcePackRepository();
        repo.updateRepositoryEntriesAll();

        List<String> savedNames = mc.gameSettings.resourcePacks;
        List<ResourcePackRepository.Entry> targetPacks = new ArrayList<>();

        Map<String, ResourcePackRepository.Entry> availableMap = new HashMap<>();
        Map<String, ResourcePackRepository.Entry> fuzzyMap = new HashMap<>();

        for (ResourcePackRepository.Entry entry : repo.getRepositoryEntriesAll()) {
            String name = entry.getResourcePackName();
            availableMap.put(name, entry);
            fuzzyMap.put(stripSpecialChars(name), entry);
        }

        System.out.println("[WorthClient] Verificando " + savedNames.size() + " texturas...");

        boolean needsUpdate = false;

        for (String savedName : savedNames) {
            if (savedName.equalsIgnoreCase("default")) continue;

            if (availableMap.containsKey(savedName)) {
                targetPacks.add(availableMap.get(savedName));
                continue;
            }

            String cleanSaved = stripSpecialChars(savedName);
            if (fuzzyMap.containsKey(cleanSaved)) {
                ResourcePackRepository.Entry match = fuzzyMap.get(cleanSaved);
                System.out.println("[WorthClient] Recuperado: " + savedName + " -> " + match.getResourcePackName());
                targetPacks.add(match);
                needsUpdate = true;
            }
        }

        if (!needsUpdate && isPackListAlreadyApplied(repo, targetPacks)) {
            System.out.println("[WorthClient] Texturas já estão sincronizadas. Pulando reload.");
            firstLoadDone = true;
            return;
        }

        if (!targetPacks.isEmpty()) {
            System.out.println("[WorthClient] Aplicando texturas...");
            try {
                repo.setRepositories(targetPacks);

                if (!firstLoadDone) {
                    System.out.println("[WorthClient] Definindo texturas para o boot inicial (sem reload forçado).");
                    firstLoadDone = true;
                } else {
                    TextureOptimizer.optimizeAndReload();
                    System.out.println("[WorthClient] Texturas aplicadas com sucesso!");
                }

            } catch (Exception e) {
                System.err.println("[WorthClient] Erro ao aplicar texturas: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean isPackListAlreadyApplied(ResourcePackRepository repo, List<ResourcePackRepository.Entry> targetPacks) {
        List<ResourcePackRepository.Entry> currentActivePacks = repo.getRepositoryEntries();
        if (currentActivePacks.size() != targetPacks.size()) return false;

        for (int i = 0; i < currentActivePacks.size(); i++) {
            if (!currentActivePacks.get(i).getResourcePackName().equals(targetPacks.get(i).getResourcePackName())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTextureActive(String textureName) {
        List<ResourcePackRepository.Entry> activePacks = Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
        String cleanSearch = stripSpecialChars(textureName);
        for (ResourcePackRepository.Entry entry : activePacks) {
            if (entry.getResourcePackName().equals(textureName)) return true;
            if (stripSpecialChars(entry.getResourcePackName()).equals(cleanSearch)) return true;
        }
        return false;
    }

    private static String stripSpecialChars(String input) {
        if (input == null) return "";
        int idx = input.lastIndexOf('.');
        String nameOnly = (idx > 0) ? input.substring(0, idx) : input;
        return nameOnly.replaceAll("[^a-zA-Z0-9]", "");
    }
}