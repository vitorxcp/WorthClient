package com.vitorxp.WorthClient.rpc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DiscordRPC {
    private static final String PIPE_PREFIX = "\\\\.\\pipe\\discord-ipc-";
    private static final int HEARTBEAT_INTERVAL = 15;

    private static volatile boolean running = true;
    private static RandomAccessFile pipe;
    private static String clientId;
    private static int pipeIndex = 0;

    private static String lastState = "No world";
    private static String lastDetails = "Jogando Minecraft 1.8.9";
    private static volatile boolean ready = false;

    private static ScheduledExecutorService heartbeatExecutor;
    private static Thread readerThread;

    public static void start(String clientIdParam) {
        clientId = clientIdParam;
        running = true;
        new Thread(DiscordRPC::mainLoop, "DiscordRPC-Main").start();
    }

    public static void stop() {
        running = false;
        if (heartbeatExecutor != null) heartbeatExecutor.shutdownNow();
        if (readerThread != null) readerThread.interrupt();
        closePipe();
        System.out.println("[DiscordRPC] Desconectado do Discord.");
    }

    public static void updateActivity(String state, String details) {
        lastState = state;
        lastDetails = details;
    }

    private static void mainLoop() {
        while (running) {
            try {
                if (pipe == null) {
                    connect();
                    sendHandshake();
                    startReaderThread();
                    waitForReady();
                    Thread.sleep(200);
                    startHeartbeat();
                }

                updateFromMinecraft();
                Thread.sleep(1000);
            } catch (IOException e) {
                System.out.println("[DiscordRPC] Erro ou pipe fechado: " + e.getMessage());
                ready = false;
                closePipe();
                incrementPipeIndex();
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            } catch (InterruptedException ignored) {}
        }
    }

    private static void updateFromMinecraft() throws IOException {
        if (!ready) return;

        Minecraft mc = Minecraft.getMinecraft();
        WorldClient world = mc.theWorld;

        if (world != null)
            updateActivity(world.getWorldInfo().getWorldName(), "Jogando Minecraft 1.8.9");
        else
            updateActivity("No world", "Jogando Minecraft 1.8.9");

        sendActivity(lastState, lastDetails);
    }

    private static void connect() throws IOException {
        int attempts = 0;
        while (attempts < 10 && pipe == null) {
            try {
                pipe = new RandomAccessFile(PIPE_PREFIX + pipeIndex, "rw");
                System.out.println("[DiscordRPC] Conectado ao pipe: " + PIPE_PREFIX + pipeIndex);
                return;
            } catch (IOException e) {
                incrementPipeIndex();
                attempts++;
            }
        }
        if (pipe == null) throw new IOException("Não conseguiu conectar a nenhum pipe.");
    }

    private static void incrementPipeIndex() {
        pipeIndex++;
        if (pipeIndex > 9) pipeIndex = 0;
    }

    private static void sendHandshake() throws IOException {
        String json = "{ \"v\": 1, \"client_id\": \"" + clientId + "\" }";
        sendMessage(0, json);
        System.out.println("[DiscordRPC] Handshake enviado: " + json);
    }

    private static void waitForReady() {
        while (!ready && running) {
            try { Thread.sleep(50); } catch (InterruptedException ignored) {}
        }
    }

    private static void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(() -> {
            if (pipe != null && ready) {
                try { sendHeartbeat(); } catch (IOException e) {
                    ready = false;
                    closePipe();
                }
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    private static void startReaderThread() {
        readerThread = new Thread(() -> {
            byte[] header = new byte[8];
            while (running && pipe != null) {
                try {
                    if (pipe.read(header) != 8) continue;
                    ByteBuffer buffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);
                    int opcode = buffer.getInt();
                    int length = buffer.getInt();
                    byte[] payload = new byte[length];
                    pipe.readFully(payload);
                    String json = new String(payload, "UTF-8");

                    if (json.contains("\"evt\":\"READY\"")) {
                        ready = true;
                        System.out.println("[DiscordRPC] Recebido READY do Discord!");
                    } else if (json.contains("\"evt\":\"CLOSE\"") || json.contains("\"evt\":\"ERROR\"")) {
                        ready = false;
                        closePipe();
                    }
                } catch (IOException e) {
                    ready = false;
                    closePipe();
                }
            }
        }, "DiscordRPC-Reader");
        readerThread.start();
    }

    private static void sendHeartbeat() throws IOException {
        sendMessage(1, "{}");
    }

    private static void sendActivity(String state, String details) throws IOException {
        if (!ready || pipe == null) return;

        long timestamp = System.currentTimeMillis() / 1000L;
        long pid = getProcessId();
        String nonce = UUID.randomUUID().toString();

        String json = "{\n" +
                "  \"cmd\": \"SET_ACTIVITY\",\n" +
                "  \"args\": {\n" +
                "    \"activity\": {\n" +
                "      \"details\": \"" + details + "\",\n" +
                "      \"state\": \"" + state + "\",\n" +
                "      \"timestamps\": { \"start\": " + timestamp + " },\n" +
                "      \"assets\": {\n" +
                "         \"large_image\": \"logo\",\n" +
                "         \"large_text\": \"Minecraft 1.8.9\"\n" +
                "      }\n" +
                "    },\n" +
                "    \"pid\": " + pid + "\n" +
                "  },\n" +
                "  \"nonce\": \"" + nonce + "\"\n" +
                "}";

        System.out.println("[DiscordRPC] Enviando activity: " + json);
        sendMessage(2, json);
    }

    private static void sendMessage(int opcode, String json) throws IOException {
        if (pipe == null) throw new IOException("Pipe não conectado.");
        byte[] data = json.getBytes("UTF-8");
        ByteBuffer buffer = ByteBuffer.allocate(8 + data.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(opcode);
        buffer.putInt(data.length);
        buffer.put(data);
        buffer.flip();
        pipe.write(buffer.array());
        pipe.getFD().sync();
    }

    private static void closePipe() {
        try { if (pipe != null) pipe.close(); } catch (IOException ignored) {}
        pipe = null;
    }

    private static long getProcessId() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }
}
