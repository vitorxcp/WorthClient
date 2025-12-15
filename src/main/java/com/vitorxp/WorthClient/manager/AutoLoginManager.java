package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AutoLoginManager {

    private static final File FILE = new File(Minecraft.getMinecraft().mcDataDir, "WorthClient/autologin.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static List<ServerConfig> servers = new ArrayList<>();

    public static void load() {
        if (!FILE.exists()) return;
        try (Reader reader = new FileReader(FILE)) {
            servers = GSON.fromJson(reader, new TypeToken<List<ServerConfig>>(){}.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (servers == null) servers = new ArrayList<>();
    }

    public static void save() {
        try (Writer writer = new FileWriter(FILE)) {
            GSON.toJson(servers, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServerConfig getServerConfig(String serverIP) {
        for (ServerConfig cfg : servers) {
            if (serverIP.toLowerCase().contains(cfg.serverIP.toLowerCase())) {
                return cfg;
            }
        }
        return null;
    }

    public static class ServerConfig {
        public String serverIP;
        public String loginCommand;
        public List<AccountEntry> accounts = new ArrayList<>();

        public ServerConfig(String ip, String cmd) {
            this.serverIP = ip;
            this.loginCommand = cmd;
        }

        public String getPassword(String username) {
            for (AccountEntry acc : accounts) {
                if (acc.username.equalsIgnoreCase(username)) {
                    return acc.password;
                }
            }
            return null;
        }

        public void addAccount(String user, String pass) {
            accounts.removeIf(a -> a.username.equalsIgnoreCase(user));
            accounts.add(new AccountEntry(user, pass));
        }
    }

    public static class AccountEntry {
        public String username;
        public String password;

        public AccountEntry(String u, String p) {
            this.username = u;
            this.password = p;
        }
    }
}