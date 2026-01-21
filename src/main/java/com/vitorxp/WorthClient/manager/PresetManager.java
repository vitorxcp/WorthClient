package com.vitorxp.WorthClient.manager;

import com.vitorxp.WorthClient.gui.utils.NotificationRenderer;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PresetManager {

    private static final File PRESET_DIR = new File("config/WorthClient/presets");
    private static final File CONFIG_FILE = new File("config/WorthClient/settings.json");

    public static void savePreset(String name) {
        String safeName = name.replaceAll("[^a-zA-Z0-9.-]", "_");

        if (!PRESET_DIR.exists()) {
            PRESET_DIR.mkdirs();
        }

        File presetFile = new File(PRESET_DIR, safeName + ".json");

        ConfigManager.save();

        if (!CONFIG_FILE.exists()) {
            NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Erro: Configuração base não encontrada!");
            return;
        }

        try {
            copyFile(CONFIG_FILE, presetFile);
            NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Preset '" + name + "' salvo!");
        } catch (IOException e) {
            e.printStackTrace();
            NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Erro ao salvar preset.");
        }
    }

    public static void loadPreset(String name) {
        File presetFile = new File(PRESET_DIR, name + ".json");

        if (!presetFile.exists()) {
            NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Preset não existe!");
            return;
        }

        try {
            copyFile(presetFile, CONFIG_FILE);
            ConfigManager.load();
            NotificationRenderer.send(NotificationRenderer.Type.SUCCESS, "Preset carregado!");
        } catch (IOException e) {
            e.printStackTrace();
            NotificationRenderer.send(NotificationRenderer.Type.ERROR, "Erro ao ler arquivo.");
        }
    }

    public static List<String> getPresetList() {
        List<String> names = new ArrayList<>();
        if (PRESET_DIR.exists()) {
            File[] files = PRESET_DIR.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".json")) {
                        names.add(f.getName().replace(".json", ""));
                    }
                }
            }
        }
        return names;
    }

    private static void copyFile(File source, File dest) throws IOException {
        try (InputStream is = new FileInputStream(source); OutputStream os = new FileOutputStream(dest)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }
}