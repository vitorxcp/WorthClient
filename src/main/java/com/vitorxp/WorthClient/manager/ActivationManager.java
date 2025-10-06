package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vitorxp.WorthClient.CryptoUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class ActivationManager {
    private static final File FILE = new File("config/WorthClient/key.json");
    public static boolean isActivated = false;
    private static String savedKey = null;
    private static Timer timer;

    public static void init() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
                isActivated = obj.has("activated") && obj.get("activated").getAsBoolean();
                if (obj.has("key")) {
                    try {
                        savedKey = CryptoUtils.decrypt(obj.get("key").getAsString());
                    } catch (Exception e) {
                        savedKey = null;
                    }
                }
            } catch (Exception ignored) {}
        }

        if (savedKey != null) {
            validateKey(savedKey);
            startAutoValidation();
        }
    }

    public static void saveActivation(boolean activated, String key) {
        JsonObject obj = new JsonObject();
        obj.addProperty("activated", activated);
        try {
            String encryptedKey = CryptoUtils.encrypt(key);
            obj.addProperty("key", encryptedKey);
        } catch (Exception e) {
            e.printStackTrace();
            obj.addProperty("key", "");
        }
        try (FileWriter writer = new FileWriter(FILE)) {
            new Gson().toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void attemptActivation(String key) {
        new Thread(() -> {
            try {
                URL url = new URL("http://ovh-vin-01.elgaehost.com.br:8280/api/check?key=" + key);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    isActivated = true;
                    savedKey = key;
                    saveActivation(true, key);
                    send("§aChave ativada com sucesso!");
                    startAutoValidation();
                } else {
                    isActivated = false;
                    saveActivation(false, key);
                    send("§cChave inválida ou expirou.");
                }
            } catch (Exception e) {
                send("§cOcorreu um erro ao entrar em contato com a API!");
                isActivated = false;
                e.printStackTrace();
            }
        }).start();
    }

    private static void validateKey(String key) {
        new Thread(() -> {
            try {
                URL url = new URL("http://ovh-vin-01.elgaehost.com.br:8280/api/check?key=" + key);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    isActivated = true;
                } else {
                    isActivated = false;
                    send("§cA chave ativada expirou ou foi revogada.");
                }
            } catch (Exception e) {
                send("§cOcorreu um erro ao entrar em contato com a API, tentando novamente em 3 minutos!");
                isActivated = false;
            }
        }).start();
    }

    public static void startAutoValidation() {
        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (savedKey != null) {
                    validateKey(savedKey);
                }
            }
        }, 3 * 60 * 1000, 5 * 60 * 1000);
    }

    private static void send(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(("§6[WorthClient] §r" + msg)));
        }
    }
}
