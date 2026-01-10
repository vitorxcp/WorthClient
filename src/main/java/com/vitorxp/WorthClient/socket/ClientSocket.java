package com.vitorxp.WorthClient.socket;

import com.vitorxp.WorthClient.gui.GuiSocial;
import com.vitorxp.WorthClient.manager.NotificationManager;
import com.vitorxp.WorthClient.social.SocialManager;
import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientSocket {

    public static Socket socket;

    public static Map<String, Set<String>> playerCosmetics = new ConcurrentHashMap<>();

    private static boolean isConnecting = false;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static boolean hasCosmetic(String nick, String cosmeticId) {
        if (nick == null) return false;
        Set<String> cosmetics = playerCosmetics.get(nick.toLowerCase());
        return cosmetics != null && cosmetics.contains(cosmeticId);
    }

    public static void connect() {
        if (isConnecting) return;
        isConnecting = true;

        new Thread(() -> {
            try {
                System.out.println("[WorthClient] Iniciando conexão Socket.IO...");

                IO.Options options = new IO.Options();
                options.transports = new String[] { "websocket", "polling" };
                options.reconnection = true;
                options.reconnectionDelay = 2000;
                options.reconnectionAttempts = 9999;
                options.timeout = 5000;

                String myNick = "Player";
                String uuid = "00000000-0000-0000-0000-000000000000";
                try {
                    if (Minecraft.getMinecraft().getSession() != null) {
                        myNick = Minecraft.getMinecraft().getSession().getUsername();
                        uuid = Minecraft.getMinecraft().getSession().getPlayerID();
                    }
                } catch (Exception ignored) {}

                options.query = "nick=" + myNick + "&uuid=" + uuid;

                socket = IO.socket("http://elgae-sp1-b001.elgaehost.com.br:9099", options);

                socket.on(Socket.EVENT_CONNECT, args -> {
                    System.out.println("[WorthClient] SUCESSO: Conectado ao Socket!");
                    socket.emit("game:launch");
                });

                socket.on(Socket.EVENT_DISCONNECT, args -> {
                    System.out.println("[WorthClient] AVISO: Desconectado do Socket.");
                });

                socket.on("init:data", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONObject) {
                            JSONObject data = (JSONObject) args[0];
                            JSONArray friendsJson = data.optJSONArray("friends");

                            SocialManager.friends.clear();
                            if (friendsJson != null) {
                                for (int i = 0; i < friendsJson.length(); i++) {
                                    JSONObject f = friendsJson.getJSONObject(i);
                                    SocialManager.friends.add(new SocialManager.Friend(
                                            f.getString("nick"),
                                            f.optString("status", "offline"),
                                            f.optBoolean("hasUnread", false)
                                    ));
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });

                socket.on("ticket:list_update", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONArray) {
                            JSONArray arr = (JSONArray) args[0];
                            SocialManager.tickets.clear();
                            for(int i = 0; i < arr.length(); i++) {
                                JSONObject t = arr.getJSONObject(i);
                                SocialManager.tickets.add(new SocialManager.Ticket(
                                        t.optString("id"),
                                        t.optString("subject"),
                                        t.optString("status"),
                                        t.optString("owner"),
                                        t.optBoolean("hasUnread", false)
                                ));
                            }
                        }
                    } catch(Exception e) { e.printStackTrace(); }
                });

                socket.on("chat:history", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONObject) {
                            JSONObject data = (JSONObject) args[0];

                            String target = data.optString("friend");
                            if(target == null || target.isEmpty()) target = data.optString("ticketId");
                            if(target == null || target.isEmpty()) target = data.optString("id");

                            JSONArray msgs = data.getJSONArray("messages");

                            if (target != null && !target.isEmpty()) {
                                SocialManager.chatHistory.put(target, new ArrayList<>());
                                for (int i = 0; i < msgs.length(); i++) {
                                    JSONObject m = msgs.getJSONObject(i);
                                    SocialManager.addMessage(target, new SocialManager.ChatMessage(
                                            m.getString("sender"),
                                            m.getString("text"),
                                            m.optLong("timestamp", System.currentTimeMillis()),
                                            m.optBoolean("read", true)
                                    ));
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });

                socket.on("chat:receive", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONObject) {
                            JSONObject msg = (JSONObject) args[0];
                            String sender = msg.getString("sender");
                            String text = msg.getString("text");
                            long time = System.currentTimeMillis();

                            SocialManager.addMessage(sender, new SocialManager.ChatMessage(sender, text, time, false));

                            Minecraft.getMinecraft().addScheduledTask(() -> {
                                if (Minecraft.getMinecraft().thePlayer != null) {
                                    Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0f, 1.0f);
                                }

                                boolean isGuiOpen = Minecraft.getMinecraft().currentScreen instanceof com.vitorxp.WorthClient.gui.GuiSocial;
                                boolean isChattingWithPerson = isGuiOpen && sender.equals(SocialManager.currentChatTarget);

                                if (!isChattingWithPerson) {
                                    com.vitorxp.WorthClient.manager.NotificationManager.show("Mensagem de " + sender, text);
                                }
                            });
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });

                socket.on("ticket:receive", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONObject) {
                            JSONObject msg = (JSONObject) args[0];
                            String ticketId = msg.getString("ticketId");
                            String sender = msg.getString("sender");
                            String text = msg.getString("text");

                            if(sender.equals(Minecraft.getMinecraft().getSession().getUsername())) return;

                            SocialManager.addMessage(ticketId, new SocialManager.ChatMessage(sender, text, System.currentTimeMillis(), false));

                            boolean isGuiOpen = Minecraft.getMinecraft().currentScreen instanceof GuiSocial;
                            boolean isChattingTicket = isGuiOpen && ticketId.equals(SocialManager.currentChatTarget);

                            if (!isChattingTicket) {
                                NotificationManager.show("Ticket #" + ticketId, text);
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });

                socket.on("friend:status_update", args -> {
                    try {
                        if (args.length > 0 && args[0] instanceof JSONObject) {
                            JSONObject data = (JSONObject) args[0];
                            String nick = data.getString("nick");
                            String status = data.getString("status");
                            for (SocialManager.Friend f : SocialManager.friends) {
                                if (f.nick.equalsIgnoreCase(nick)) {
                                    f.status = status;
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                });

                socket.on("client:users_playing", args -> {
                    try {
                        Map<String, Set<String>> newMap = new ConcurrentHashMap<>();

                        if (args.length > 0 && args[0] instanceof JSONArray) {
                            JSONArray arr = (JSONArray) args[0];

                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject playerData = arr.getJSONObject(i);
                                String pNick = playerData.getString("nick").toLowerCase();
                                JSONArray pCosmetics = playerData.optJSONArray("cosmetics");

                                Set<String> activeIds = new HashSet<>();
                                if (pCosmetics != null) {
                                    for (int j = 0; j < pCosmetics.length(); j++) {
                                        activeIds.add(pCosmetics.getString(j));
                                    }
                                }
                                newMap.put(pNick, activeIds);
                            }
                        }

                        playerCosmetics.putAll(newMap);
                        playerCosmetics = newMap;

                        updateLocalPlayerCosmetics();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                socket.on("client:cosmetics:player", args -> {
                    if (args.length == 0 || !(args[0] instanceof Boolean) || !(boolean) args[0]) return;

                    try {
                        String myName = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
                        Set<String> myActiveCosmetics = new HashSet<>();

                        for (int i = 1; i < args.length; i++) {
                            Object obj = args[i];
                            if (obj instanceof JSONObject) {
                                JSONObject cosmetic = (JSONObject) obj;
                                String name = cosmetic.optString("name");
                                if (name != null && !name.isEmpty()) {
                                    myActiveCosmetics.add(name);
                                }
                            }
                        }

                        playerCosmetics.put(myName, myActiveCosmetics);

                        System.out.println("[WorthClient] Meus cosméticos carregados: " + myActiveCosmetics);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                socket.connect();
                startBackgroundTasks();
                addShutdownHook();
            } catch (Exception e) {
                e.printStackTrace();
                isConnecting = false;
            }
        }).start();
    }

    private static void updateLocalPlayerCosmetics() {
        try {
            if (Minecraft.getMinecraft().getSession() != null) {
                String me = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
                playerCosmetics.putIfAbsent(me, new HashSet<>());
            }
        } catch (Exception ignored) {}
    }

    private static void startBackgroundTasks() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (socket == null) return;

                if (!socket.connected()) {
                    System.out.println("[WorthClient] Watchdog: Socket off. Reconectando...");
                    socket.connect();
                } else {
                    socket.emit("game:ping");
                }
            } catch (Exception e) {
                System.err.println("[WorthClient] Erro no Watchdog: " + e.getMessage());
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (socket != null && socket.connected()) {
                    System.out.println("[WorthClient] Fechando: Enviando game:close...");
                    socket.emit("game:close");
                    try { Thread.sleep(200); } catch (InterruptedException e) {}
                    socket.disconnect();
                }
                scheduler.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }
}