package com.vitorxp.SkyBlockModVX.hud;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HudPositionManager {
    private static final File FILE = new File("config/SkyBlockModVX/hud_positions.json");
    public static Map<String, HudElement> elements = new HashMap<>();

    public static void registerElement(HudElement element) {
        elements.put(element.id, element);
    }

    public static HudElement get(String id) {
        return elements.get(id);
    }

    public static void load() {
        elements.clear(); // garantir que tá limpo

        if (!FILE.exists()) {
            System.out.println("Arquivo HUD não encontrado: " + FILE.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //System.out.println("Linha lida: " + line);
                String[] parts = line.split("=");

                if (parts.length < 2) {
                    System.out.println("Ignorando linha inválida: " + line);
                    continue;
                }

                String[] coords = parts[1].split(",");
                if (coords.length < 2) {
                    System.out.println("Coordenadas inválidas: " + parts[1]);
                    continue;
                }

                String id = parts[0];
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);

                HudElement element = new HudElement(id, x, y);
                elements.put(id, element);

                System.out.println("Adicionado: " + id + " -> " + x + "," + y);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            if (!FILE.getParentFile().exists()) FILE.getParentFile().mkdirs();
            PrintWriter writer = new PrintWriter(FILE);

            for (HudElement el : elements.values()) {
                writer.println(el.id + "=" + el.x + "," + el.y);
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}