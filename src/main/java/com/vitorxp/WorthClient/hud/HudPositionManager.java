package com.vitorxp.WorthClient.hud;

import java.awt.Point;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HudPositionManager {
    private static final File FILE = new File("config/WorthClient/hud_positions.json");
    private static final Map<String, Point> positions = new HashMap<>();

    public static Point getPosition(String id) {
        return positions.get(id);
    }

    public static void savePosition(HudElement element) {
        positions.put(element.id, new Point(element.x, element.y));
        save();
    }

    public static void load() {
        positions.clear();
        if (!FILE.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length < 2) continue;

                String[] coords = parts[1].split(",");
                if (coords.length < 2) continue;

                String id = parts[0];
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                positions.put(id, new Point(x, y));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            FILE.getParentFile().mkdirs();
            try (PrintWriter writer = new PrintWriter(FILE)) {
                for (Map.Entry<String, Point> entry : positions.entrySet()) {
                    writer.println(entry.getKey() + ":" + entry.getValue().x + "," + entry.getValue().y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}