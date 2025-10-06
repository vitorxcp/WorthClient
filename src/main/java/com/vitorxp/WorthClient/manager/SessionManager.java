package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

public class SessionManager {

    private static final Gson gson = new Gson();
    private static final java.io.File SESSION_CACHE_FILE = new java.io.File(Minecraft.getMinecraft().mcDataDir, "session_cache.json");

    public void saveSession(Session session) {
        try (FileWriter writer = new FileWriter(SESSION_CACHE_FILE)) {
            gson.toJson(session, writer);
            System.out.println("Sessão salva com sucesso em: " + SESSION_CACHE_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Falha ao salvar a sessão!");
            e.printStackTrace();
        }
    }

    public Session loadSession() {
        if (!SESSION_CACHE_FILE.exists()) {
            System.out.println("Nenhum arquivo de cache de sessão encontrado.");
            return null;
        }

        try (FileReader reader = new FileReader(SESSION_CACHE_FILE)) {
            Session loadedSession = gson.fromJson(reader, Session.class);
            System.out.println("Sessão carregada com sucesso para o usuário: " + loadedSession.getUsername());
            return loadedSession;
        } catch (IOException e) {
            System.err.println("Falha ao carregar a sessão!");
            e.printStackTrace();
            return null;
        }
    }

    public void applyCachedSession() {
        System.out.println("Servidores de autenticação parecem estar offline. Tentando usar sessão em cache...");
        Session cachedSession = loadSession();

        if (cachedSession != null) {
            try {
                Class<?> minecraftClass = Minecraft.getMinecraft().getClass();

                Field sessionField;
                try {
                    sessionField = minecraftClass.getDeclaredField("session");
                } catch (NoSuchFieldException e) {
                    sessionField = minecraftClass.getDeclaredField("field_71449_j");
                }

                sessionField.setAccessible(true);

                sessionField.set(Minecraft.getMinecraft(), cachedSession);

                System.out.println("Sessão em cache aplicada com sucesso! O jogo agora tentará se conectar com a sessão antiga.");

            } catch (Exception e) {
                System.err.println("Falha ao aplicar a sessão em cache usando Reflection!");
                e.printStackTrace();
            }
        } else {
            System.err.println("Não foi possível aplicar a sessão em cache.");
        }
    }
}
