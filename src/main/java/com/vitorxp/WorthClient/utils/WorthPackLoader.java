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
        List<ResourcePackRepository.Entry> packsToActivate = new ArrayList<>();

        System.out.println("[WorthClient] Analisando " + savedNames.size() + " texturas salvas...");

        for (String savedName : savedNames) {
            if (savedName.equals("Default") || savedName.equals("default")) continue;

            boolean found = false;

            for (ResourcePackRepository.Entry entry : repo.getRepositoryEntriesAll()) {
                if (entry.getResourcePackName().equals(savedName)) {
                    packsToActivate.add(entry);
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
                        packsToActivate.add(entry);
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                System.out.println("[WorthClient] FALHA FATAL: Não achei o arquivo para: [" + savedName + "]");
            }
        }

        if (!packsToActivate.isEmpty()) {
            repo.setRepositories(packsToActivate);
            mc.refreshResources();
            System.out.println("[WorthClient] " + packsToActivate.size() + " texturas aplicadas com sucesso!");
        }
    }

    private static String stripSpecialChars(String input) {
        if (input == null) return "";
        String noZip = input.replace(".zip", "").replace(".ZIP", "");
        return noZip.replaceAll("[^a-zA-Z0-9]", "");
    }
}