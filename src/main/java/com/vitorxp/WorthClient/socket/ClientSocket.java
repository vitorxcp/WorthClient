package com.vitorxp.WorthClient.socket;

import io.socket.client.IO;
import io.socket.client.Socket;
import net.minecraft.client.Minecraft;
import org.json.JSONArray;

import java.util.HashSet;
import java.util.Set;

public class ClientSocket {

    public static Socket socket;
    public static Set<String> usersUsingClient = new HashSet<>();

    public static void connect() {
        new Thread(() -> {
            try {
                System.out.println("[WorthClient] Iniciando conexão Socket.IO (v2.1.0)...");

                IO.Options options = new IO.Options();
                options.transports = new String[] { "websocket", "polling" };
                options.reconnection = true;
                options.timeout = 5000;

                String myNick = "Player";
                String uuid = "00000000";

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
                });

                socket.on("client:users_playing", args -> {
                    try {
                        System.out.println("SOCKET RECEBEU DADOS: " + args[0]);

                        usersUsingClient.clear();
                        JSONArray arr = (JSONArray) args[0];
                        for (int i = 0; i < arr.length(); i++) {
                            usersUsingClient.add(arr.getString(i).toLowerCase());
                        }

                        usersUsingClient.add(Minecraft.getMinecraft().getSession().getUsername().toLowerCase());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                socket.connect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static boolean isUserOnClient(String username) {
        if (usersUsingClient == null || username == null) return false;
        return usersUsingClient.contains(username.toLowerCase());
    }
}