package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AutoTextManager {

    private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "WorthClient/autotext.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<TextMacro> macros = new ArrayList<>();

    public static void load() {
        if (!FILE.exists()) return;
        try (Reader reader = new FileReader(FILE)) {
            macros = GSON.fromJson(reader, new TypeToken<List<TextMacro>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (macros == null) macros = new ArrayList<>();
    }

    public static void save() {
        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(macros, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class TextMacro {
        public String message;
        public int keyCode;

        public TextMacro(String message, int keyCode) {
            this.message = message;
            this.keyCode = keyCode;
        }
    }
}