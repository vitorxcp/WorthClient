package com.vitorxp.WorthClient.social;

import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocialManager {

    public static String myStatus = "online";
    public static List<Friend> friends = new CopyOnWriteArrayList<>();
    public static List<Ticket> tickets = new CopyOnWriteArrayList<>();

    // Usamos Collections.synchronizedMap para garantir segurança básica no mapa
    public static Map<String, List<ChatMessage>> chatHistory = Collections.synchronizedMap(new HashMap<>());

    public static String currentChatTarget = null;
    public static boolean isTicketChat = false;

    // Executor para salvar arquivos em fila, sem criar 300 threads ao mesmo tempo
    private static final ExecutorService saveExecutor = Executors.newSingleThreadExecutor();

    static {
        loadHistory();
    }

    public static class Friend {
        public String nick, status;
        public boolean hasUnread;
        public Friend(String n, String s, boolean unread) {
            this.nick = n; this.status = s; this.hasUnread = unread;
        }
    }

    public static class Ticket {
        public String id, subject, status, owner;
        public boolean hasUnread;

        public Ticket(String id, String subj, String stat, String owner, boolean hasUnread) {
            this.id = id;
            this.subject = subj;
            this.status = stat;
            this.owner = owner;
            this.hasUnread = hasUnread;
        }
    }

    public static class ChatMessage {
        public String sender, text;
        public long timestamp;
        public boolean read;

        public ChatMessage(String s, String t, long time, boolean r) {
            this.sender = s; this.text = t; this.timestamp = time; this.read = r;
        }
    }

    public static void addMessage(String targetId, ChatMessage msg) {
        synchronized (chatHistory) {
            chatHistory.putIfAbsent(targetId, new ArrayList<>()); // ArrayList interna protegida pelo synchronized
            chatHistory.get(targetId).add(msg);
            // Ordenar aqui dentro para evitar conflito
            chatHistory.get(targetId).sort(Comparator.comparingLong(m -> m.timestamp));
        }
        saveHistory();
    }

    public static List<ChatMessage> getMessages(String targetId) {
        synchronized (chatHistory) {
            if (!chatHistory.containsKey(targetId)) return new ArrayList<>();
            // Retorna uma CÓPIA da lista para evitar erro ao renderizar enquanto uma msg nova chega
            return new ArrayList<>(chatHistory.get(targetId));
        }
    }

    public static void clearHistory(String targetId) {
        chatHistory.remove(targetId);
        saveHistory();
    }

    public static void loadHistory() {
        try {
            File file = new File(Minecraft.getMinecraft().mcDataDir, "social_history.json");
            if (!file.exists()) return;

            String content = new String(Files.readAllBytes(file.toPath()));
            JSONObject json = new JSONObject(content);

            synchronized (chatHistory) {
                Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    JSONArray msgsArray = json.getJSONArray(key);
                    List<ChatMessage> list = new ArrayList<>();
                    for (int i = 0; i < msgsArray.length(); i++) {
                        JSONObject m = msgsArray.getJSONObject(i);
                        list.add(new ChatMessage(
                                m.getString("s"), m.getString("t"), m.getLong("ts"), m.getBoolean("r")
                        ));
                    }
                    chatHistory.put(key, list);
                }
            }
            System.out.println("[WorthClient] Histórico de chat carregado.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void saveHistory() {
        // Envia para a fila de salvamento
        saveExecutor.submit(() -> {
            try {
                JSONObject json = new JSONObject();

                // Snapshot dos dados para evitar ConcurrentModification durante o loop
                Map<String, List<ChatMessage>> snapshot;
                synchronized (chatHistory) {
                    snapshot = new HashMap<>();
                    for (Map.Entry<String, List<ChatMessage>> entry : chatHistory.entrySet()) {
                        snapshot.put(entry.getKey(), new ArrayList<>(entry.getValue()));
                    }
                }

                for (Map.Entry<String, List<ChatMessage>> entry : snapshot.entrySet()) {
                    JSONArray arr = new JSONArray();
                    for (ChatMessage m : entry.getValue()) {
                        JSONObject msgObj = new JSONObject();
                        msgObj.put("s", m.sender);
                        msgObj.put("t", m.text);
                        msgObj.put("ts", m.timestamp);
                        msgObj.put("r", m.read);
                        arr.put(msgObj);
                    }
                    json.put(entry.getKey(), arr);
                }

                File file = new File(Minecraft.getMinecraft().mcDataDir, "social_history.json");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(json.toString());
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }
}