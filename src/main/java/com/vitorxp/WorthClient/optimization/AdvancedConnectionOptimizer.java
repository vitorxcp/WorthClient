package com.vitorxp.WorthClient.optimization;

import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class AdvancedConnectionOptimizer {

    private static final Minecraft mc = Minecraft.getMinecraft();

    private static final AtomicLong lastPingTime = new AtomicLong(0);
    private static String currentServer = null;

    private static final int PING_INTERVAL = 5000;
    private static final int MAX_ACCEPTABLE_PING = 250;
    private static final int KEEP_ALIVE_INTERVAL = 10000;
    private static final int MIN_PACKET_INTERVAL = 50;

    private static long lastKeepAlive = 0;
    private static long lastPacketTime = 0;

    private static final List<String> hosts = new CopyOnWriteArrayList<>();

    static {
        hosts.add("redewortgh.com");
        hosts.add("redesky.com");
    }

    public static void start() {
        if(mc.getCurrentServerData() != null) {
            currentServer = mc.getCurrentServerData().serverIP;
        }
        new Thread(AdvancedConnectionOptimizer::pingThread).start();
    }

    private static void pingThread() {
        while (true) {
            try {
                if(currentServer != null) {
                    String host = currentServer.split(":")[0];
                    InetAddress address = InetAddress.getByName(host);
                    long start = System.currentTimeMillis();
                    boolean reachable = address.isReachable(1000);
                    long ping = System.currentTimeMillis() - start;
                    lastPingTime.set(ping);
                }
                Thread.sleep(PING_INTERVAL);
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if(mc.thePlayer == null || mc.getCurrentServerData() == null) return;

        NetworkManager nm = mc.getNetHandler().getNetworkManager();
        long now = System.currentTimeMillis();

        if(now - lastKeepAlive > KEEP_ALIVE_INTERVAL) {
            try {
                nm.sendPacket(new C00PacketKeepAlive((int) now));
            } catch (Exception ignored) {}
            lastKeepAlive = now;
        }

        if(now - lastPacketTime > MIN_PACKET_INTERVAL) {
            try {
                nm.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        mc.thePlayer.rotationYaw,
                        mc.thePlayer.rotationPitch,
                        false
                ));
            } catch (Exception ignored) {}
            lastPacketTime = now;
        }
    }

    public static String getBestHost() {
        String best = null;
        int bestPing = Integer.MAX_VALUE;

        for (String host : hosts) {
            int p = pingHost(host, 25565, 1000);
            if (p >= 0 && p < bestPing) {
                bestPing = p;
                best = host;
            }
        }

        lastPingTime.set(bestPing);
        return best != null ? best : hosts.get(0);
    }

    private static int pingHost(String host, int port, int timeout) {
        try (Socket socket = new Socket()) {
            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(host, port), timeout);
            return (int) (System.currentTimeMillis() - start);
        } catch (Exception e) {
            return -1;
        }
    }
}