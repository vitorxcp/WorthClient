package com.vitorxp.WorthClient.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientSocket {

    public static Socket socket;
    public static Set<String> usersUsingClient = new HashSet<>();
    private static boolean isConnecting = false;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

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

                options.query = "nick=" + myNick + "&uuid=" + uuid + "&status=online";

                socket = IO.socket("http://elgae-sp1-b001.elgaehost.com.br:9099", options);

                socket.on(Socket.EVENT_CONNECT, args -> {
                    System.out.println("[WorthClient] SUCESSO: Conectado ao Socket!");
                    socket.emit("game:launch");
                });

                socket.on(Socket.EVENT_DISCONNECT, args -> {
                    System.out.println("[WorthClient] AVISO: Desconectado do Socket.");
                });

                socket.on("client:users_playing", args -> {
                    try {
                        usersUsingClient.clear();
                        if (args.length > 0 && args[0] instanceof JSONArray) {
                            JSONArray arr = (JSONArray) args[0];
                            for (int i = 0; i < arr.length(); i++) {
                                usersUsingClient.add(arr.getString(i).toLowerCase());
                            }
                        }
                        if (Minecraft.getMinecraft().getSession() != null) {
                            usersUsingClient.add(Minecraft.getMinecraft().getSession().getUsername().toLowerCase());
                        }
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