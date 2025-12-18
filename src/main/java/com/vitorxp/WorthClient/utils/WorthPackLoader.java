package com.vitorxp.WorthClient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ResourcePackRepository;
import java.util.ArrayList;
import java.util.List;

public class WorthPackLoader {

    public static void reloadSavedPacks() {
        Minecraft mc = Minecraft.getMinecraft();
        ResourcePackRepository repo = mc.getResourcePackRepository();
        repo.updateRepositoryEntriesAll();
        List<String> savedNames = mc.gameSettings.resourcePacks;
        List<ResourcePackRepository.Entry> targetPacks = new ArrayList<>();
        System.out.println("[WorthClient] Analisando " + savedNames.size() + " texturas salvas...");

        for (String savedName : savedNames) {
            if (savedName.equals("Default") || savedName.equals("default")) continue;
            boolean found = false;
            for (ResourcePackRepository.Entry entry : repo.getRepositoryEntriesAll()) {
                if (entry.getResourcePackName().equals(savedName)) {
                    targetPacks.add(entry);
                    found = true;
                    break;
                }
            }

            if (!found) {
                String cleanSaved = stripSpecialChars(savedName);
                for (ResourcePackRepository.Entry entry : repo.getRepositoryEntriesAll()) {
                    String realName = entry.getResourcePackName();
                    String cleanReal = stripSpecialChars(realName);
                    if (!cleanSaved.isEmpty() && cleanSaved.equals(cleanReal)) {
                        System.out.println("[WorthClient] Recuperado (Fuzzy Match): " + savedName + " -> " + realName);
                        targetPacks.add(entry);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                System.out.println("[WorthClient] FALHA FATAL: Não achei o arquivo para: [" + savedName + "]");
            }
        }

        if (isPackListAlreadyApplied(repo, targetPacks)) {
            System.out.println("[WorthClient] As texturas salvas já estão aplicadas. Pulando reload.");
            return;
        }

        if (!targetPacks.isEmpty()) {
            System.out.println("[WorthClient] Aplicando novas texturas...");
            repo.setRepositories(targetPacks);
            mc.refreshResources();
            System.out.println("[WorthClient] " + targetPacks.size() + " texturas aplicadas com sucesso!");
        }
    }

    private static boolean isPackListAlreadyApplied(ResourcePackRepository repo, List<ResourcePackRepository.Entry> targetPacks) {
        List<ResourcePackRepository.Entry> currentActivePacks = repo.getRepositoryEntries();
        if (currentActivePacks.size() != targetPacks.size()) {
            return false;
        }

        for (int i = 0; i < currentActivePacks.size(); i++) {
            String activeName = currentActivePacks.get(i).getResourcePackName();
            String targetName = targetPacks.get(i).getResourcePackName();
            if (!activeName.equals(targetName)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isTextureActive(String textureName) {
        List<ResourcePackRepository.Entry> activePacks = Minecraft.getMinecraft().getResourcePackRepository().getRepositoryEntries();
        for (ResourcePackRepository.Entry entry : activePacks) {
            if (entry.getResourcePackName().equals(textureName)) {
                return true;
            }
            if (stripSpecialChars(entry.getResourcePackName()).equals(stripSpecialChars(textureName))) {
                return true;
            }
        }
        return false;
    }

    private static String stripSpecialChars(String input) {
        if (input == null) return "";
        String noZip = input.replace(".zip", "").replace(".ZIP", "");
        return noZip.replaceAll("[^a-zA-Z0-9]", "");
    }
}